package com.cheekymammoth.input;

public interface IInteractable {
	int getInputFocus();
	void didGainFocus();
	void willLoseFocus();
	void update(CMInputs input);
}
