package innovimax.quixproc.datamodel.in;

import javax.xml.transform.Source;

public abstract class AStreamSource {
	enum Type {XML, JSON}
	protected final Type type;
	protected AStreamSource(Type type) {
		this.type = type;
	}
	public static AStreamSource instance(Source source) {
		return new XMLStreamSource(source);
	}
	
	public static class XMLStreamSource extends AStreamSource {
		public final Source source;
		private XMLStreamSource(Source source) {
			super(Type.XML);
			this.source = source;
		}		
	}
 
}
