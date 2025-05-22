package Custom;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class CanonicalBoard {

	public enum Symmetry {
		IDENTITY((Integer i, Integer j) -> copy(i, j)), ROTATE_90((Integer i, Integer j) -> rotate90(i, j)),
		ROTATE_180((Integer i, Integer j) -> rotate180(i, j)), ROTATE_270((Integer i, Integer j) -> rotate270(i, j)),
		REFLECT_HORIZONTAL((Integer i, Integer j) -> reflectHorizontal(i, j)),
		REFLECT_VERTICAL((Integer i, Integer j) -> reflectVertical(i, j)),
		REFLECT_DIAGONAL_MAIN((Integer i, Integer j) -> reflectDiagonalMain(i, j)),
		REFLECT_DIAGONAL_ANTI((Integer i, Integer j) -> reflectDiagonalAnti(i, j));

		private BiFunction<Integer, Integer, Integer> transformation;

		private Symmetry(BiFunction<Integer, Integer, Integer> transformation) {
			this.transformation = transformation;
		}

		public Symmetry getInverse() {
			switch (this) {
			case ROTATE_90:
				return ROTATE_270;
			case ROTATE_270:
				return ROTATE_90;
			case REFLECT_DIAGONAL_MAIN:
				return REFLECT_DIAGONAL_MAIN;
			case REFLECT_DIAGONAL_ANTI:
				return REFLECT_DIAGONAL_ANTI;
			default:
				return this; // IDENTITY, ROTATE_180, REFLECT_HORIZONTAL, REFLECT_VERTICAL
			}
		}

		public Pawn[][] applyToBoard(Pawn[][] board) {
			int n = board.length;
			Pawn[][] result = new Pawn[n][n];
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					int newIndexes = transformation.apply(i, j);
					result[newIndexes / 10][newIndexes % 10] = board[i][j];
				}
			return result;
		}

		private static int copy(int i, int j) {
			return i * 10 + j;
		}

		private static int rotate90(int i, int j) {
			return j * 10 + 9 - 1 - i;
		}

		private static int rotate180(int i, int j) {
			int res = rotate90(i, j);
			return rotate90(res / 10, res % 10);
		}

		private static int rotate270(int i, int j) {
			int res = rotate180(i, j);
			return rotate90(res / 10, res % 10);
		}

		private static int reflectHorizontal(int i, int j) {
			return (9 - 1 - i) * 10 + j;
		}

		private static int reflectVertical(int i, int j) {
			return i * 10 + 9 - 1 - j;
		}

		private static int reflectDiagonalMain(int i, int j) {
			return j * 10 + i;
		}

		private static int reflectDiagonalAnti(int i, int j) {
			return (9 - 1 - j) * 10 + 9 - 1 - i;
		}

		public Action applyToAction(Action a) {
			int rowFrom = a.getRowFrom();
			int colFrom = a.getColumnFrom();
			int rowTo = a.getRowTo();
			int colTo = a.getColumnTo();
			int reversedFrom = transformation.apply(rowFrom, colFrom);
			int reversedTo = transformation.apply(rowTo, colTo);
			Action applied = null;
			try {
				applied = new Action(((char) ((reversedFrom % 10) + 97) + "" + ((reversedFrom / 10)+1)),
						((char) ((reversedTo % 10) + 97) + "" + ((reversedTo / 10)+1)), a.getTurn());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return applied;
		}

	}

	private Symmetry applied;
	private Symmetry inverse;
	private Pawn[][] canonical;

	private CanonicalBoard(Symmetry applied, Symmetry inverse, Pawn[][] canonical) {
		super();
		this.applied = applied;
		this.inverse = inverse;
		this.canonical = canonical;
	}

	public static CanonicalBoard from(Pawn[][] original) {
		return findCanonical(original);
	}

	private static CanonicalBoard findCanonical(Pawn[][] board) {
	    String bestKey = null;
	    CanonicalBoard best = null;
		Symmetry[] values = Symmetry.values();
		for (int h = 0; h < values.length; h++) {
			Pawn[][] newBoard = values[h].applyToBoard(board);
			StringBuilder sb = new StringBuilder();
		    for (Pawn[] row : newBoard)
		        for (Pawn cell : row)
		            sb.append(cell.ordinal());
		    String key = sb.toString();
	        if (bestKey == null || key.compareTo(bestKey) < 0) {
	            bestKey = key;
	            best = new CanonicalBoard(values[h], values[h].getInverse(), newBoard);
	        }		}
		return best;
	}

	public Symmetry getApplied() {
		return applied;
	}

	public Symmetry getInverse() {
		return inverse;
	}

	public Pawn[][] getCanonical() {
		return canonical;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(canonical);
		result = prime * result + Objects.hash(applied, inverse);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CanonicalBoard other = (CanonicalBoard) obj;
		return applied == other.applied && Arrays.deepEquals(canonical, other.canonical) && inverse == other.inverse;
	}

}
