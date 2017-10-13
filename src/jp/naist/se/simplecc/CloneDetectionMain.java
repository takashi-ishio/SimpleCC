package jp.naist.se.simplecc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

/**
 * A simple type-2 clone detector.
 * @author Takashi Ishio
 */
public class CloneDetectionMain {
	
	private static int MIN_TOKENS = 20;

	public static void main(String[] args) {
		ArrayList<CodeToken> tokens = readFiles(args);
		detectClones(tokens, MIN_TOKENS);
	}
	
	private static ArrayList<CodeToken> readFiles(String[] args) {
		ArrayList<CodeToken> tokens = new ArrayList<>();
		for (String arg: args) {
			DirectoryScan.scan(new File(arg), new DirectoryScan.Action() {
				@Override
				public void process(File f) {
					if (f.getName().toLowerCase().endsWith(".java")) {
						try {
							Java8Lexer lexer = new Java8Lexer(CharStreams.fromPath(f.toPath()));
							for (Token t = lexer.nextToken(); t.getType() != Lexer.EOF; t = lexer.nextToken()) {
								CodeToken token = new CodeToken(t.getText(), getNormalizedText(t), f, t.getLine(), t.getCharPositionInLine());
								tokens.add(token);
							}
							tokens.add(CodeToken.getTerminalToken());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		return tokens;
	}
	
	private static String getNormalizedText(Token t) {
		switch (t.getType()) {
		case Java8Lexer.Identifier:
		case Java8Lexer.INT:
		case Java8Lexer.LONG:
		case Java8Lexer.FLOAT:
		case Java8Lexer.DOUBLE:
		case Java8Lexer.SHORT:
		case Java8Lexer.BOOLEAN:
		case Java8Lexer.BYTE:
		case Java8Lexer.VOID:
			return "$p";
		default:
			return t.getText();	
		}
	}
	
	private static void detectClones(ArrayList<CodeToken> tokens, int threshold) {
		boolean[][] checked = new boolean[tokens.size()][tokens.size()];
		for (int i=0; i<tokens.size(); i++) {
			for (int j=i+1; j<tokens.size(); j++) {
				if (checked[i][j]) continue; 
				
				if (tokens.get(i).isSameToken(tokens.get(j))) {
					int match = 0;
					while (tokens.get(i + match).isSameToken(tokens.get(j + match))) {
						match++;
						checked[i+match][j+match] = true;
					}
					if (match >= threshold) {
						reportClone(tokens, i, j, match);
					}
				}
			}
		}
	}
	
	private static void reportClone(ArrayList<CodeToken> tokens, int start1, int start2, int length) {
		System.out.println("<pair>");
		printCode(tokens, start1, length);
		printCode(tokens, start2, length);
		System.out.println("</pair>");
	}
	
	private static void printCode(ArrayList<CodeToken> tokens, int start, int length) { 
		CodeToken startToken = tokens.get(start);
		CodeToken endToken = tokens.get(start + length - 1);
		assert startToken.getFile() == endToken.getFile();
		System.out.print(startToken.getFile().getAbsolutePath() + "," + startToken.getLine() + "," + startToken.getCharPositionInLine() + "," + endToken.getLine() + "," + endToken.getEndCharPositionInLine() + ",");
		for (int i=0; i<length; i++) {
			System.out.print(tokens.get(start + i).getText() + "\t");
		}
		System.out.println();
	}

}
