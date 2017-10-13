package jp.naist.se.simplecc;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CodeToken {

	private String text;
	private File file;
	private int line;
	private int charPositionInLine;
	private long hash;

	private static MessageDigest digest;
	
	static {
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public CodeToken(String text, String normalized, File f, int line, int charPositionInLine) {
		this.text = text;
		this.file = f;
		this.line = line;
		this.charPositionInLine = charPositionInLine;
		if (normalized != null) {
			this.hash = getHash(normalized);
		}
	}
	
	public static CodeToken getTerminalToken() {
		return new CodeToken(null, null, null, 0, 0);
	}
	
	private long getHash(String s) {
		digest.update(s.getBytes());
		byte[] b = digest.digest();
		long hash = 0;
		for (int i=0; i<8; i++) {
			hash = (hash << 8) + (long)b[i];
		}
		return hash;
	}
	
	public boolean isSameToken(CodeToken another) {
		return this.text != null && another.text != null && this.hash == another.hash;
	}
	
	public String getText() {
		return text;
	}
	
	public File getFile() {
		return file;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getCharPositionInLine() {
		return charPositionInLine;
	}

	public int getEndCharPositionInLine() {
		return charPositionInLine + text.length();
	}
	
	@Override
	public String toString() {
		return file.getAbsolutePath() + "," + line + "," + charPositionInLine + "," + text + "," + hash;
	}
}
