package com.cheekymammoth.files;

public interface IFileClient {
	public static final int SAVE_SUCCEEDED = 1;
	public static final int SAVE_FAILED = 2;
	void onAsyncSaveCompleted(final int saveResult);
}
