package innovimax.quixproc.datamodel.generator;


public abstract class ATreeGenerator extends AGenerator {
	public enum Type {
		HIGH_DENSITY, 
		HIGH_DEPTH, 
		HIGH_ELEMENT_NAME_SIZE_SINGLE,
		HIGH_ELEMENT_NAME_SIZE_OPEN_CLOSE,
		HIGH_TEXT_SIZE,
		SPECIFIC
	}
	private Type treeType;

	protected ATreeGenerator(FileExtension type, Type treeType) {
		super(type);
		this.treeType = treeType;
	}

	protected ATreeGenerator(FileExtension type) {
		super(type);
		this.treeType = Type.SPECIFIC;
	}
	
	public static abstract class AHighDensityGenerator extends ATreeGenerator {


		public AHighDensityGenerator(FileExtension type) {
			super(type, Type.HIGH_DENSITY);
		}

		@Override
		protected int updatePattern(int current_pattern) {
			return (current_pattern + 1) % getPatterns().length;
		}

		@Override
		protected long updateSize(long current_size, int current_pattern) {
			return current_size + getPatterns()[current_pattern].length;
		}

		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			return current_size < total;
		}

	}

	public static abstract class AHighDepthGenerator extends ATreeGenerator {
	
	
		protected AHighDepthGenerator(FileExtension ext, ATreeGenerator.Type gtype) {
			super(ext, gtype);
		}
	
		private int next_pattern = 0;
	
		@Override
		protected int updatePattern(int current_pattern) {
			// return internal state
			return this.next_pattern;
		}
	
		@Override
		protected long updateSize(long current_size, int current_pattern) {
			// update the size by adding open and closing tag
			return current_size + (current_pattern == 0 ? getPatternsLength() : 0);
		}
	
		private long loop = 0;
	
		@Override
		protected boolean notFinished(long current_size, int current_pattern, long total) {
			// System.out.println(current_size + ", "+current_pattern+",
			// "+total);
			if (current_size < total) {
				loop++;
				return true;
			}
			// current_size >= total
			if (current_pattern == 0) {
				// switch pattern
				this.next_pattern = 1;
			}
			// next_pattern will be 1
			return this.loop-- > 0;
		}
	
		protected abstract int getPatternsLength();
	
	}


}
