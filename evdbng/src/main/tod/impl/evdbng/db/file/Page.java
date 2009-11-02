package tod.impl.evdbng.db.file;

import java.lang.ref.WeakReference;

public abstract class Page
{
	/**
	 * Can keep a cache of decompressed tuples.
	 */
	private WeakReference<TupleBuffer<?>> itsTupleBuffer;
	
	/**
	 * Logical page id.
	 */
	private int itsPageId;
	
	public Page(int aPageId)
	{
		itsPageId = aPageId;
	}

	public int getPageId()
	{
		return itsPageId;
	}
	
	protected void invalidate()
	{
		itsPageId = -1;
	}
	
	public abstract PagedFile getFile();

	public abstract void use();
	public abstract void free();

	/**
	 * Returns the cached tuple buffer, if present.
	 */
	public TupleBuffer<?> getTupleBuffer()
	{
		return itsTupleBuffer != null ? itsTupleBuffer.get() : null;
	}
	
	/**
	 * Caches a tuple buffer in this page (weakly referenced).
	 */
	public void setTupleBuffer(TupleBuffer<?> aTupleBuffer)
	{
		itsTupleBuffer = aTupleBuffer != null ? new WeakReference<TupleBuffer<?>>(aTupleBuffer) : null;
	}
	
	public abstract boolean readBoolean(int aPosition);
	public abstract void writeBoolean(int aPosition, boolean aValue);
	public abstract void readBytes(int aPosition, byte[] aBuffer, int aOffset, int aCount);
	public abstract void writeBytes(int aPosition, byte[] aBytes, int aOffset, int aCount);
	public abstract byte readByte(int aPosition);
	public abstract void writeByte(int aPosition, int aValue);
	public abstract short readShort(int aPosition);
	public abstract void writeShort(int aPosition, int aValue);
	public abstract int readInt(int aPosition);
	public abstract void writeInt(int aPosition, int aValue);
	public abstract long readLong(int aPosition);
	public abstract void writeLong(int aPosition, long aValue);
	public abstract void writeBB(int aPosition, int aByte1, int aByte2);
	public abstract void writeBS(int aPosition, int aByte, int aShort);
	public abstract void writeBI(int aPosition, int aByte, int aInt);
	public abstract void writeBL(int aPosition, int aByte, long aLong);
	public abstract void writeInternalTupleData(int aPosition, int aPageId, long aTupleCount);
	
	public abstract int getPageSize();
	
	public PageIOStream asIOStream()
	{
		return new PageIOStream(this);
	}
	
	@Override
	public String toString()
	{
		StringBuilder theBuilder = new StringBuilder("[\n");
		PageIOStream theBitStruct = asIOStream();
		for (int i=0;i<getPageSize()/16;i++)
		{
			theBuilder.append("  ("+i*16+") | ");
			
			for (int j=0;j<16;j++)
			{
				String theHexString = Integer.toHexString(theBitStruct.readByte() & 0xff);
				if (theHexString.length() == 1) theBuilder.append('0');
				theBuilder.append(theHexString);
				theBuilder.append(' ');
			}
			
			theBuilder.append('\n');
		}
		theBuilder.append("]");
		return theBuilder.toString();
	}

	/**
	 * Provides a stream view of the page, with a current position and the ability to read/write
	 * basic data types.
	 * @author gpothier
	 */
	public static class PageIOStream 
	{
		private final Page itsPage;
		private int itsPosition;

		public PageIOStream(Page aPage)
		{
			assert aPage != null;
			itsPage = aPage;
			setPos(0);
		}

		public Page getPage()
		{
			return itsPage;
		}
	
		public static int booleanSize()
		{ 
			return 1;
		}
		
		public boolean readBoolean()
		{
			boolean theValue = itsPage.readBoolean(getPos());
			skip(1);
			return theValue;
		}
		
		public void writeBoolean(boolean aValue)
		{
			itsPage.writeBoolean(getPos(), aValue);
			skip(1);
		}

		public void writeBytes(byte[] aBytes)
		{
			writeBytes(aBytes, 0, aBytes.length);
		}
		
		public void readBytes(byte[] aBuffer, int aOffset, int aCount)
		{
			itsPage.readBytes(getPos(), aBuffer, aOffset, aCount);
			skip(aCount);
		}
		
		public void writeBytes(byte[] aBytes, int aOffset, int aCount)
		{
			itsPage.writeBytes(getPos(), aBytes, aOffset, aCount);
			skip(aCount);
		}

		public static int byteSize()
		{ 
			return 1;
		}
		
		public byte readByte()
		{
			byte theValue = itsPage.readByte(getPos());
			skip(1);
			return theValue;
		}
		
		public void writeByte(int aValue)
		{
			itsPage.writeByte(getPos(), aValue);
			skip(1);
		}
		
		public static int shortSize()
		{ 
			return 2;
		}
		
		public short readShort()
		{
			short theValue = itsPage.readShort(getPos());
			skip(2);
			return theValue;
		}
		
		/**
		 * Reads an unsigned short.
		 */
		public int readUShort()
		{
			return readShort() & 0xffff;
		}
		
		public void writeShort(int aValue)
		{
			itsPage.writeShort(getPos(), aValue);
			skip(2);
		}
		
		public static int intSize()
		{ 
			return 4;
		}
		
		public int readInt()
		{
			int theValue = itsPage.readInt(getPos());
			skip(4);
			return theValue;
		}
		
		public void writeInt(int aValue)
		{
			itsPage.writeInt(getPos(), aValue);
			skip(4);
		}

		public static int longSize()
		{ 
			return 8;
		}
		
		public long readLong()
		{
			long theValue = itsPage.readLong(getPos());
			skip(8);
			return theValue;
		}
		
		public void writeLong(long aValue)
		{
			itsPage.writeLong(getPos(), aValue);
			skip(8);
		}
		
		public void writeBB(int aByte1, int aByte2)
		{
			itsPage.writeBB(getPos(), aByte1, aByte2);
			skip(2);
		}

		public void writeBS(int aByte, int aShort)
		{
			itsPage.writeBS(getPos(), aByte, aShort);
			skip(3);
		}
		
		public void writeBI(int aByte, int aInt)
		{
			itsPage.writeBI(getPos(), aByte, aInt);
			skip(5);
		}
		
		public void writeBL(int aByte, long aLong)
		{
			itsPage.writeBL(getPos(), aByte, aLong);
			skip(9);
		}

		public static int internalTupleDataSize()
		{
			return PageIOStream.pagePointerSize()+PageIOStream.tupleCountSize();
		}
		
		public void writeInternalTupleData(int aPageId, long aTupleCount)
		{
			itsPage.writeInternalTupleData(getPos(), aPageId, aTupleCount);
			skip(internalTupleDataSize());
		}

		public static int behaviorIdSize()
		{
			return 4;
		}
		
		public int readBehaviorId()
		{
			return readInt();
		}
		
		public void writeBehaviorId(int aId)
		{
			writeInt(aId);
		}
		
		public static int fieldIdSize()
		{
			return 4;
		}
		
		public int readFieldId()
		{
			return readInt();
		}
		
		public void writeFieldId(int aId)
		{
			writeInt(aId);
		}
		
		public static int variableIdSize()
		{
			return 2;
		}
		
		public int readVariableId()
		{
			return readUShort();
		}
		
		public void writeVariableId(int aId)
		{
			writeShort(aId);
		}
		
		public static int typeIdSize()
		{
			return 2;
		}
		
		public int readTypeId()
		{
			return readUShort();
		}
		
		public void writeTypeId(int aId)
		{
			writeShort(aId);
		}
		
		public static int threadIdSize()
		{
			return 2;
		}
		
		public int readThreadId()
		{
			return readUShort();
		}
		
		public void writeThreadId(int aId)
		{
			writeShort(aId);
		}
		
		public static int cflowDepthSize()
		{
			return 2;
		}
		
		public int readCFlowDepth()
		{
			return readUShort();
		}
		
		public void writeCFlowDepth(int aDepth)
		{
			writeShort(aDepth);
		}
		
		public static int bytecodeIndexSize()
		{
			return 2;
		}
		
		public int readBytecodeIndex()
		{
			return readUShort();
		}
		
		public void writeBytecodeIndex(int aIndex)
		{
			writeShort(aIndex);
		}
		
		public static int adviceSourceIdSize()
		{
			return 2;
		}
		
		public int readAdviceSourceId()
		{
			return readUShort();
		}
		
		public void writeAdviceSourceId(int aId)
		{
			writeShort(aId);
		}
		
		public static int timestampSize()
		{
			return 8;
		}
		
		public long readTimestamp()
		{
			return readLong();
		}
		
		public void writeTimestamp(long aTimestamp)
		{
			writeLong(aTimestamp);
		}
		
		public static int pagePointerSize()
		{
			return 4;
		}
		
		public int readPagePointer()
		{
			return readInt();
		}
		
		public void writePagePointer(int aPointer)
		{
			writeInt(aPointer);
		}
		
		public static int tupleCountSize()
		{
			return 8;
		}
		
		public long readTupleCount()
		{
			return readLong();
		}
		
		public void writeTupleCount(long aCount)
		{
			writeLong(aCount);
		}
		
		public static int pageOffsetSize()
		{
			return 2;
		}
		
		public short readPageOffset()
		{
			return readShort();
		}
		
		public void writePageOffset(int aOffset)
		{
			writeShort(aOffset);
		}
		
		public static int eventPointerSize()
		{
			return 8;
		}
		
		public long readEventPointer()
		{
			return readLong();
		}
		
		public void writeEventPointer(long aPointer)
		{
			writeLong(aPointer);
		}
		
		public static int roleSize()
		{
			return 1;
		}
		
		public int readRole()
		{
			return readByte();
		}
		
		public void writeRole(int aRole)
		{
			writeByte(aRole);
		}
		
		public static int probeIdSize()
		{
			return 4;
		}
		
		public int readProbeId()
		{
			return readInt();
		}
		
		public void writeProbeId(int aId)
		{
			writeInt(aId);
		}
		
		/**
		 * The current position of this struct's pointer, in bytes.
		 */
		public int getPos()
		{
			return itsPosition;
		}
		
		/**
		 * Sets the current position of this struct's pointer, in bytes.
		 */
		public void setPos(int aPos)
		{
			itsPosition = aPos;
			assert itsPosition <= itsPage.getPageSize();
		}
		
		public void rewind()
		{
			setPos(0);
		}
		
		public void skip(int aCount)
		{
			setPos(getPos()+aCount);
		}
		
		/**
		 * The size of the underlying page, in bytes.
		 */
		public int size()
		{
			return itsPage.getPageSize();
		}
		
		/**
		 * Number of bytes remaining to read or write in this stream.
		 */
		public int remaining()
		{
			return size() - getPos();
		}
	}

	/**
	 * Similar to a {@link PageIOStream} but supports overflowing by chaining pages.
	 * Previous page and next page pointers are stored at the end of the page. 
	 * @author gpothier
	 */
	public static class ChainedPageIOStream
	{
		private final PagedFile itsFile;
		private PageIOStream itsCurrentStream;
		
		public ChainedPageIOStream(PagedFile aFile)
		{
			itsFile = aFile;
			itsCurrentStream = itsFile.create().asIOStream();
		}
		
		public ChainedPageIOStream(PagedFile aFile, int aPageId, int aPosition)
		{
			itsFile = aFile;
			itsCurrentStream = itsFile.get(aPageId).asIOStream();
			itsCurrentStream.setPos(aPosition);
		}
		
		/**
		 * Returns the current page.
		 */
		public Page getCurrentPage()
		{
			return itsCurrentStream.getPage();
		}
		
		public PageIOStream getCurrentStream()
		{
			return itsCurrentStream;
		}

		/**
		 * Checks that the current page has enough space to hold aSpace more bytes.
		 * If it does not, a new page is allocated and chained to this one.
		 */
		private void checkSpace(int aSpace)
		{
			assert aSpace < itsFile.getPageSize()-2*PageIOStream.pagePointerSize();
			if (itsCurrentStream.remaining()-2*PageIOStream.pagePointerSize() >= aSpace) return;
			
			Page theNewPage = itsFile.create();
			
			// Write next page id on current page
			itsCurrentStream.setPos(itsFile.getPageSize()-PageIOStream.pagePointerSize());
			itsCurrentStream.writePagePointer(theNewPage.getPageId());
			
			// Write previous page id on next page
			PageIOStream theNewStruct = theNewPage.asIOStream();
			theNewStruct.setPos(itsFile.getPageSize()-2*PageIOStream.pagePointerSize());
			int theOldPageId = itsCurrentStream.getPage().getPageId();
			theNewStruct.writePagePointer(theOldPageId);
			
			theNewStruct.rewind();
			itsCurrentStream = theNewStruct;
			
			newPageHook(theOldPageId, theNewPage.getPageId());
		}

		/**
		 * Hook that can be used to be notified when a new page is allocated
		 * @param aNewPageId Id of the newly allocated page. 
		 */
		protected void newPageHook(int aOldPageId, int aNewPageId)
		{
		}
		
		public void writeByte(int aValue, int aDataSpace)
		{
			checkSpace(1+aDataSpace);
			itsCurrentStream.writeByte(aValue);
		}
		
		public void writeShort(int aValue, int aDataSpace)
		{
			checkSpace(2+aDataSpace);
			itsCurrentStream.writeShort(aValue);
		}
		
		public void writeInt(int aValue, int aDataSpace)
		{
			checkSpace(4+aDataSpace);
			itsCurrentStream.writeInt(aValue);
		}
		
		public void writeLong(long aValue, int aDataSpace)
		{
			checkSpace(8+aDataSpace);
			itsCurrentStream.writeLong(aValue);
		}
		
		public void writeBB(int aByte1, int aByte2, int aDataSpace)
		{
			checkSpace(2+aDataSpace);
			itsCurrentStream.writeBB(aByte1, aByte2);
		}

		public void writeBS(int aByte, int aShort, int aDataSpace)
		{
			checkSpace(3+aDataSpace);
			itsCurrentStream.writeBS(aByte, aShort);
		}
		
		public void writeBI(int aByte, int aInt, int aDataSpace)
		{
			checkSpace(5+aDataSpace);
			itsCurrentStream.writeBI(aByte, aInt);
		}
		
		public void writeBL(int aByte, long aLong, int aDataSpace)
		{
			checkSpace(9+aDataSpace);
			itsCurrentStream.writeBL(aByte, aLong);
		}
		
		public void writeInternalTupleData(int aPageId, long aTupleCount)
		{
			checkSpace(PageIOStream.internalTupleDataSize());
			itsCurrentStream.writeInternalTupleData(aPageId, aTupleCount);
		}
	}

}
