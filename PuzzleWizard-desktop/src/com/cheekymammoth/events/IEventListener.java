package com.cheekymammoth.events;

public interface IEventListener {
	public static final int INVALID_EV_TYPE = Integer.MIN_VALUE;
	// Converting evData: http://www.java-forums.org/new-java/31633-equivalent-void-java.html
	public void onEvent(int evType, Object evData);
}
