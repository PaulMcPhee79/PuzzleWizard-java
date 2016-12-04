package com.cheekymammoth.puzzleFactories;

import com.badlogic.gdx.utils.Pools;
import com.cheekymammoth.puzzleViews.AnimPlayerPiece;
import com.cheekymammoth.puzzleViews.HumanPlayerPiece;
import com.cheekymammoth.puzzleViews.MirrorPlayerPiece;
import com.cheekymammoth.puzzles.HumanPlayer;
import com.cheekymammoth.puzzles.MirroredPlayer;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.puzzles.Player.PlayerType;
import com.cheekymammoth.utils.Coord;

public class PlayerFactory {

	private PlayerFactory() { }
	
	public static Player getPlayer(PlayerType type) {
		Player player = null;
		
		switch (type) {
			case HUMAN: player = Pools.obtain(HumanPlayer.class); break;
			case MIRRORED: player = Pools.obtain(MirroredPlayer.class); break;
			default:
				assert(false) : "Invalid arg in PlayerFactory::getPlayer(PlayerType type)";
				break;
		}
		
		return player;
	}
	
	public static Player getPlayer(PlayerType type, int colorKey, Coord position, int orientation) {
		Player player = getPlayer(type);
		player.devInit(colorKey, position, orientation);
		return player;
	}
	
	public static HumanPlayer getHumanPlayer() {
		return (HumanPlayer)getPlayer(PlayerType.HUMAN);
	}
	
	public static HumanPlayer getHumanPlayer(int colorKey, Coord position, int orientation) {
		return (HumanPlayer)getPlayer(PlayerType.HUMAN, colorKey, position, orientation);
	}
	
	public static MirroredPlayer getMirroredPlayer() {
		return (MirroredPlayer)getPlayer(PlayerType.MIRRORED);
	}
	
	public static MirroredPlayer getMirroredPlayer(int colorKey, Coord position, int orientation) {
		return (MirroredPlayer)getPlayer(PlayerType.MIRRORED, colorKey, position, orientation);
	}
	
	public static void freePlayer(Player player) {
		if (player != null)
			Pools.free(player);
	}
	
	public static AnimPlayerPiece getPlayerPiece(Player player) {
		if (player != null) {
			AnimPlayerPiece playerPiece = player.getType() == PlayerType.HUMAN
		    		? getHumanPlayerPiece(player)
		    	    : getMirrorPlayerPiece(player);
			playerPiece.setData(player);
			return playerPiece;
		}
		
		return null;
	}
	
	private static AnimPlayerPiece getHumanPlayerPiece(Player player) {
		HumanPlayerPiece playerPiece = Pools.obtain(HumanPlayerPiece.class);
		playerPiece.setData(player);
		return playerPiece;
	}
	
	private static AnimPlayerPiece getMirrorPlayerPiece(Player player) {
		MirrorPlayerPiece playerPiece = Pools.obtain(MirrorPlayerPiece.class);
		playerPiece.setData(player);
		return playerPiece;
	}
	
	public static void freePlayerPiece(AnimPlayerPiece playerPiece) {
		if (playerPiece != null)
			Pools.free(playerPiece);
	}
}
