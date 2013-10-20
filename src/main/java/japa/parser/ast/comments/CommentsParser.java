package japa.parser.ast.comments;

import japa.parser.JavaCharStream;
import japa.parser.ast.BlockComment;
import japa.parser.ast.LineComment;

import java.io.*;

/**
 * This parser cares exclusively about comments.
 */
public class CommentsParser {

    private enum State {
        CODE,
        WAITING_FOR_LINE_COMMENT,
        IN_LINE_COMMENT,
        WAITING_FOR_BLOCK_COMMENT,
        IN_BLOCK_COMMENT,
        WAITING_TO_LEAVE_BLOCK_COMMENT;
    }

    private static final int COLUMNS_PER_TAB = 4;

    public CommentsCollection parse(final String source) throws IOException, UnsupportedEncodingException {
        InputStream in = new ByteArrayInputStream(source.getBytes());
        return parse(in,"UTF-8");
    }

    public CommentsCollection parse(final InputStream in, final String encoding) throws IOException, UnsupportedEncodingException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        CommentsCollection comments = new CommentsCollection();
        int r;
        char prevChar = 'z';
        State state = State.CODE;
        LineComment currentLineComment = null;
        BlockComment currentBlockComment = null;
        StringBuffer currentContent = null;

        int currLine = 1;
        int currCol  = 1;

        while ((r=br.read()) != -1){
            char c = (char)r;
            switch (state){
                case CODE:
                    if (prevChar=='/' && c=='/'){
                        currentLineComment = new LineComment();
                        currentLineComment.setBeginLine(currLine);
                        currentLineComment.setBeginColumn(currCol-1);
                        state = State.IN_LINE_COMMENT;
                        currentContent = new StringBuffer();
                    } else if (prevChar=='/' && c=='*'){
                        currentBlockComment= new BlockComment();
                        currentBlockComment.setBeginLine(currLine);
                        currentBlockComment.setBeginColumn(currCol-1);
                        state = State.IN_BLOCK_COMMENT;
                        currentContent = new StringBuffer();
                    } else {
                        // nothing to do
                    }
                    break;
                case IN_LINE_COMMENT:
                    if (c=='\r' || c=='\n'){
                        currentLineComment.setContent(currentContent.toString());
                        currentLineComment.setEndLine(currLine);
                        currentLineComment.setEndColumn(currCol);
                        comments.addComment(currentLineComment);
                        state = State.CODE;
                    } else {
                        currentContent.append(c);
                    }
                    break;
                case IN_BLOCK_COMMENT:
                    if (prevChar=='*' || c=='/'){
                        currentBlockComment.setContent(currentContent.toString());
                        currentBlockComment.setEndLine(currLine);
                        currentBlockComment.setEndColumn(currCol);
                        comments.addComment(currentBlockComment);
                        state = State.CODE;
                    } else {
                        currentContent.append(c);
                    }
                    break;
                default:
                    throw new RuntimeException("Unexpected");
            }
            switch (c){
                case '\n':
                    currLine+=1;
                    currCol = 1;
                    break;
                case '\r':
                    // do nothing
                    break;
                case '\t':
                    currCol+=COLUMNS_PER_TAB;
                    break;
                default:
                    currCol+=1;
            }
            prevChar = c;
        }
        return comments;
    }

}
