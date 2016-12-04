package com.cheekymammoth.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.cheekymammoth.utils.Coord;

public class InputManager {
	private static InputManager singleton = new InputManager();
	
	private boolean enabled = true;
	private boolean isBusyUpdatingClients;
	private int focusMap;
	private IntArray focusStack = new IntArray(true, 10);
	private Array<IInteractable> clients = new Array<IInteractable>(true, 10, IInteractable.class);
	private Array<IInteractable> subscribeQueue = new Array<IInteractable>(true, 10, IInteractable.class);
	private Array<IInteractable> unsubscribeQueue = new Array<IInteractable>(true, 10, IInteractable.class);
	
	private int modalFocusMap;
	private IntArray modalFocusStack = new IntArray(true, 10);
	private Array<IInteractable> modalClients = new Array<IInteractable>(true, 10, IInteractable.class);
	private Array<IInteractable> modalSubscribeQueue = new Array<IInteractable>(true, 10, IInteractable.class);
	private Array<IInteractable> modalUnsubscribeQueue = new Array<IInteractable>(true, 10, IInteractable.class);
	
	private CMInputs controllerState = new CMInputs();
	
	private InputManager() {
		// Input keys are also map keys, so must be unique.
		int[] inputKeys = new int[] {
				Input.Keys.W,
				Input.Keys.S,
				Input.Keys.A,
				Input.Keys.D,
				Input.Keys.UP,
				Input.Keys.DOWN,
				Input.Keys.LEFT,
				Input.Keys.RIGHT,
				Input.Keys.ENTER,
				Input.Keys.SPACE,
				Input.Keys.ESCAPE,
				Input.Keys.MEDIA_PREVIOUS,
				Input.Keys.MEDIA_NEXT,
				Input.Keys.COMMA,
				Input.Keys.PERIOD,
				Input.Keys.P,
				Input.Keys.ALT_LEFT,
				Input.Keys.ALT_RIGHT
			};

		int[] ctrlInputs = new int[] {
				CMInputs.CI_UP,
				CMInputs.CI_DOWN,
				CMInputs.CI_LEFT,
				CMInputs.CI_RIGHT,
				CMInputs.CI_UP,
				CMInputs.CI_DOWN,
				CMInputs.CI_LEFT,
				CMInputs.CI_RIGHT,
				CMInputs.CI_CONFIRM_ENTER,
				CMInputs.CI_CONFIRM_SPACEBAR,
				CMInputs.CI_CANCEL,
				CMInputs.CI_PREV_SONG,
				CMInputs.CI_NEXT_SONG,
				CMInputs.CI_PREV_SONG,
				CMInputs.CI_NEXT_SONG,
				CMInputs.CI_PRT_SCR,
				CMInputs.CI_ALT,
				CMInputs.CI_ALT
			};
		
		IntIntMap keyMap = new IntIntMap(inputKeys.length);
		for (int i = 0, n = inputKeys.length; i < n; i++)
			keyMap.put(inputKeys[i], ctrlInputs[i]);
		
		ControlsManager cm = ControlsManager.CM();
		cm.registerKeys(keyMap);

		pushFocusState(CMInputs.FOCUS_STATE_NONE);
		pushFocusState(CMInputs.FOCUS_STATE_NONE, true);
	}
	
	public static InputManager IM() {
        return singleton;
    }
	
	public void enable(boolean enable) {
		this.enabled = enable;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	private boolean contains(Array<IInteractable> clients, IInteractable client) {
		if (clients != null && client != null)
			return clients.contains(client, true);
		else
			return false;
	}
	
	private void add(Array<IInteractable> clients, IInteractable client) {
		if (clients != null && client != null && !contains(clients, client))
			clients.add(client);
	}
	
	private void remove(Array<IInteractable> clients, IInteractable client) {
		if (contains(clients, client))
			clients.removeValue(client, true);
	}
	
	public void subscribe(IInteractable client) {
		subscribe(client, false);
	}
	
	public void unsubscribe(IInteractable client) {
		unsubscribe(client, false);
	}
	
	public void subscribe(IInteractable client, boolean modal) {
		if (client == null)
			return;
		
		Array<IInteractable> clients = modal ? this.modalClients : this.clients;
		Array<IInteractable> subscribeQueue = modal ? this.modalSubscribeQueue : this.subscribeQueue;
		Array<IInteractable> unsubscribeQueue = modal ? this.modalUnsubscribeQueue : this.unsubscribeQueue;
		
		if (isBusyUpdatingClients) {
			if (!contains(clients, client))
				add(subscribeQueue, client);
			remove(unsubscribeQueue, client);
		} else
			add(clients, client);
		
		if (hasFocus(client.getInputFocus()))
			client.didGainFocus();
		else
			client.willLoseFocus();
	}
	
	public void unsubscribe(IInteractable client, boolean modal) {
		if (client == null)
			return;
		
		Array<IInteractable> clients = modal ? this.modalClients : this.clients;
		Array<IInteractable> subscribeQueue = modal ? this.modalSubscribeQueue : this.subscribeQueue;
		Array<IInteractable> unsubscribeQueue = modal ? this.modalUnsubscribeQueue : this.unsubscribeQueue;
		
		if (isBusyUpdatingClients) {
			add(unsubscribeQueue, client);
			remove(subscribeQueue, client);
		} else
			remove(clients, client);
	}
	
	private CMInputs pollControllerInputs() {
		return controllerState.set(ControlsManager.CM().getInputMap());
	}
	
	public void update() {
		if (!isEnabled())
			return;
		
		pollControllerInputs();
		
		Array<IInteractable> clients = modalFocusMap != 0 ? this.modalClients : this.clients;
		
		isBusyUpdatingClients = true;
		for (int i = 0, n = clients.size; i < n; i++) {
			IInteractable client = clients.get(i);
			if (hasFocus(client.getInputFocus())) {
				client.update(controllerState);
				if (!hasFocus(client.getInputFocus()))
					break;
			}
		}
		isBusyUpdatingClients = false;
		
		for (int i = 0, n = subscribeQueue.size; i < n; i++)
			add(this.clients, subscribeQueue.get(i));
		for (int i = 0, n = unsubscribeQueue.size; i < n; i++)
			add(this.clients, unsubscribeQueue.get(i));
		subscribeQueue.clear();
		unsubscribeQueue.clear();
		
		for (int i = 0, n = modalSubscribeQueue.size; i < n; i++)
			add(this.modalClients, modalSubscribeQueue.get(i));
		for (int i = 0, n = modalUnsubscribeQueue.size; i < n; i++)
			add(this.modalClients, modalUnsubscribeQueue.get(i));
		modalSubscribeQueue.clear();
		modalUnsubscribeQueue.clear();
	}
	
	private void updateFocusMap(int focusState, boolean modal) {
		int focusMap = 0;

        switch (focusState) {
            case CMInputs.FOCUS_STATE_NONE:
                focusMap = 0;
                break;
            case CMInputs.FOCUS_STATE_TITLE:
                focusMap = CMInputs.HAS_FOCUS_TITLE;
                break;
            case CMInputs.FOCUS_STATE_MENU:
                focusMap = CMInputs.HAS_FOCUS_MENU;
                break;
            case CMInputs.FOCUS_STATE_PUZZLE_MENU:
                focusMap = CMInputs.HAS_FOCUS_PUZZLE_MENU;
                break;
            case CMInputs.FOCUS_STATE_MENU_DIALOG:
                focusMap = CMInputs.HAS_FOCUS_MENU_DIALOG;
                break;
            case CMInputs.FOCUS_STATE_PF_PLAYFIELD:
                focusMap = CMInputs.HAS_FOCUS_BOARD;
                break;
        }

        notifyFocusChange(focusMap, modal);

        if (modal)
            modalFocusMap = focusMap;
        else
            this.focusMap = focusMap;
	}
	
	public boolean hasFocus(int focus) {
		int focusMap = modalFocusMap != 0 ? modalFocusMap : this.focusMap;
        return hasFocus(focusMap, focus);
	}
	
	private boolean hasFocus(int focusMap, int focus) {
		// This is for incremental FOCUS_CAT values
//		return (focusMap & CMInputs.FOCUS_CAT_MASK) == (focus & CMInputs.FOCUS_CAT_MASK) && 
//				((focusMap & CMInputs.HAS_FOCUS_MASK) & (focus & CMInputs.HAS_FOCUS_MASK)) != 0;
		
		// This is for bitmask FOCUS_CAT values
		return ((focusMap & CMInputs.FOCUS_CAT_MASK) & (focus & CMInputs.FOCUS_CAT_MASK)) != 0 && 
				((focusMap & CMInputs.HAS_FOCUS_MASK) & (focus & CMInputs.HAS_FOCUS_MASK)) != 0;
	}
	
	private void notifyFocusChange(int focusMap, boolean modal) {
		boolean wasBusy = isBusyUpdatingClients;

		isBusyUpdatingClients = true;
        for (int i = 0; i < 2; i++) {
            // Non-modal clients don't need to know about:
                // 1. Non-modal focus states if the modal focus map is active.
                // 2. Modal focus states if they they don't toggle the modal focus map activity (on/off).
            if (i == 0 && ((!modal && modalFocusMap != 0) || (modal && focusMap != 0 && modalFocusMap != 0)))
                continue;

            // Modal clients don't need to know about non-modal focus changes.
            if (i == 1 && !modal)
                continue;

            int oldFocusMap = 0, newFocusMap = 0;

            if (i == 0) {
                if (modal && focusMap == 0 && modalFocusMap != 0) {
                    // Special case: switching from modal back down to non-modal.
                    oldFocusMap = focusMap;
                    newFocusMap = this.focusMap;
                } else {
                    oldFocusMap = this.focusMap;
                    newFocusMap = focusMap;
                }
            } else {
                oldFocusMap = modalFocusMap;
                newFocusMap = focusMap;
            }

            Array<IInteractable> clients = modalFocusMap != 0 ? this.modalClients : this.clients;
            for (int j = 0, n = clients.size; j < n; j++) {
            	IInteractable client = clients.get(j);
                boolean hadFocus = hasFocus(oldFocusMap, client.getInputFocus());
                boolean hasFocus = hasFocus(newFocusMap, client.getInputFocus());

                if (hadFocus && !hasFocus)
                    client.willLoseFocus();
                else if (!hadFocus && hasFocus)
                    client.didGainFocus();
            }

            clients = (i == 0) ? subscribeQueue : modalSubscribeQueue;
            for (int j = 0, n = clients.size; j < n; j++) {
            	IInteractable client = clients.get(j);
            	boolean hadFocus = hasFocus(oldFocusMap, client.getInputFocus());
            	boolean hasFocus = hasFocus(newFocusMap, client.getInputFocus());

                if (hadFocus && !hasFocus)
                    client.willLoseFocus();
                else if (!hadFocus && hasFocus)
                    client.didGainFocus();
            }
        }

        if (!wasBusy)
        	isBusyUpdatingClients = false;
	}
	
	public void pushFocusState(int focusState) {
		pushFocusState(focusState, false);
	}
	
	public void pushFocusState(int focusState, boolean modal) {
		IntArray focusStack = (modal) ? modalFocusStack : this.focusStack;
        int stackCount = focusStack.size;

        // Don't allow the same state to double-up on top of the stack. This would only happen when
        // clients are mismanaging states.
        if (stackCount == 0 || focusStack.get(stackCount-1) != focusState) {
            focusStack.add(focusState);
            updateFocusMap(focusState, modal);
        }
	}
	
	public void popFocusState() {
		popFocusState(CMInputs.FOCUS_STATE_NONE, false);
	}
	
	public void popFocusState(int focusState) {
		popFocusState(focusState, false);
	}
	
	public void popFocusState(int focusState, boolean modal) {
		IntArray focusStack = (modal) ? modalFocusStack : this.focusStack;
        int stackCount = focusStack.size;

        if (stackCount > 1) { // Don't pop base state
            if (focusState == CMInputs.FOCUS_STATE_NONE || focusStack.get(stackCount-1) == focusState) {
                focusStack.removeIndex(stackCount-1);
                updateFocusMap(focusStack.get(stackCount-2), modal);
            }
        }
	}
	
	public void popToFocusState(int focusState) {
		popToFocusState(focusState, false);
	}
	
	public void popToFocusState(int focusState, boolean modal) {
		IntArray focusStack = (modal) ? modalFocusStack : this.focusStack;

        while (focusStack.size > 1 && focusStack.get(focusStack.size-1) != focusState) // Don't pop base state
            popFocusState(focusStack.get(focusStack.size-1));

        // If focus state was not on the stack, then push it onto the stack.
        if (focusStack.size == 1)
            pushFocusState(focusState);
        else
            updateFocusMap(focusState, modal);
	}

	public Coord getDepressedVector() {
		return ControlsManager.CM().getDepressedVector();
	}
	
	public Coord getHeldVector() {
		return ControlsManager.CM().getHeldVector();
	}
}
