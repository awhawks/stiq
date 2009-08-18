/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.replay2;

import gnu.trove.TByteArrayList;

import org.objectweb.asm.Type;

import tod.core.config.TODConfig;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.IStructureDatabase.BehaviorMonitoringModeChange;
import tod.impl.replay.IntDeltaReceiver;
import tod.impl.replay.LongDeltaReceiver;
import tod.impl.replay.TmpIdManager;
import tod.impl.server.BufferStream;
import tod2.agent.Message;
import tod2.agent.MonitoringMode;
import tod2.agent.ValueType;
import zz.utils.ArrayStack;
import zz.utils.Stack;
import zz.utils.Utils;

public class ThreadReplayer
{
	public static final boolean ECHO = false;

	private final TODConfig itsConfig;
	private final IStructureDatabase itsDatabase;
	
	private int itsMessageCount = 0;
	private Stack<ReplayerFrame> itsStack = new ArrayStack<ReplayerFrame>();

	private BufferStream itsStream;
	private final TmpIdManager itsTmpIdManager;
	private final IntDeltaReceiver itsBehIdReceiver = new IntDeltaReceiver();
	private final LongDeltaReceiver itsObjIdReceiver = new LongDeltaReceiver();
	
	/**
	 * The monitoring modes of each behavior, indexed by behavior id.
	 * The mode is updated whenever we receive a {@link Message#TRACEDMETHODS_VERSION} message.
	 */
	private final TByteArrayList itsMonitoringModes = new TByteArrayList();
	private int itsCurrentMonitoringModeVersion = 0;

	private ExceptionInfo itsLastException;
	
	public ThreadReplayer(
			TODConfig aConfig, 
			IStructureDatabase aDatabase, 
			TmpIdManager aTmpIdManager,
			BufferStream aBuffer)
	{
		itsConfig = aConfig;
		itsDatabase = aDatabase;
		itsTmpIdManager = aTmpIdManager;
		itsStream = aBuffer;
	}
	
	public void replay()
	{
		createUnmonitoredFrame(null).invokeVoid();		
	}
	
	public IStructureDatabase getDatabase()
	{
		return itsDatabase;
	}
	
	
	public final void echo(String aText, Object... aArgs)
	{
		Utils.printlnIndented(itsStack.size()*2, aText, aArgs);
	}
	

	public byte getNextMessage()
	{
		processStatelessMessages();
		return nextMessage();
	}
	
	private byte nextMessage()
	{
		byte theMessage = itsStream.get();
		if (ECHO) echo("Message: %s [#%d @%d]", Message._NAMES[theMessage], itsMessageCount, itsStream.position());
		return theMessage;
	}
	
	public byte peekNextMessage()
	{
		processStatelessMessages();
		return itsStream.peek();
	}
	
	private void processStatelessMessages()
	{
		while(true)
		{
			byte theMessage = itsStream.peek();
			
			switch(theMessage)
			{
			case Message.TRACEDMETHODS_VERSION:
				processTracedMethodsVersion(itsStream.getInt());
				break;
				
			case Message.REGISTER_REFOBJECT:
				processRegisterRefObject(itsObjIdReceiver.receiveFull(itsStream), itsStream);
				break;
				
			case Message.REGISTER_REFOBJECT_DELTA:
				processRegisterRefObject(itsObjIdReceiver.receiveDelta(itsStream), itsStream);
				break;
				
			case Message.REGISTER_OBJECT: processRegisterObject(itsStream); break;
			case Message.REGISTER_OBJECT_DELTA: throw new UnsupportedOperationException();
			case Message.REGISTER_THREAD: processRegisterThread(itsStream); break;
			case Message.REGISTER_CLASS: processRegisterClass(itsStream); break;
			case Message.REGISTER_CLASSLOADER: processRegisterClassLoader(itsStream); break;
			case Message.SYNC: processSync(itsStream); break;
			
			default:
				return;
			}

			nextMessage();
		}
	}
	
	private void processTracedMethodsVersion(int aVersion)
	{
		for(int i=itsCurrentMonitoringModeVersion;i<aVersion;i++)
		{
			BehaviorMonitoringModeChange theChange = itsDatabase.getBehaviorMonitoringModeChange(i);
			while (itsMonitoringModes.size() <= theChange.behaviorId) itsMonitoringModes.add((byte) 0);
			itsMonitoringModes.set(theChange.behaviorId, (byte) theChange.mode);
		}
		
		itsCurrentMonitoringModeVersion = aVersion;
	}
	
	/**
	 * Returns the current monitoring mode for the given method
	 * @return One of the constants in {@link MonitoringMode}.
	 */
	public int getBehaviorMonitoringMode(int aBehaviorId)
	{
		if (aBehaviorId >= itsMonitoringModes.size()) return 0;
		else return itsMonitoringModes.getQuick(aBehaviorId);
	}
	
	private void processRegisterObject(BufferStream aBuffer)
	{
		int theDataSize = aBuffer.getInt();
		long theId = aBuffer.getLong();
		boolean theIndexable = aBuffer.get() != 0;
		
		byte[] theData = new byte[theDataSize];
		aBuffer.get(theData, 0, theDataSize);
		
		//TODO: register object
	}
	
	private void processRegisterRefObject(long aId, BufferStream aBuffer)
	{
		int theClassId = aBuffer.getInt();
		
		// TODO: register object
	}
	
	private void processRegisterThread(BufferStream aBuffer)
	{
		long theId = aBuffer.getLong();
		String theName = aBuffer.getString();
		
		// TODO: register
	}
	
	private void processRegisterClass(BufferStream aBuffer)
	{
		int theClassId = aBuffer.getInt();
		long theLoaderId = aBuffer.getLong();
		String theName = aBuffer.getString();
		
		// TODO: register
	}
	
	private void processRegisterClassLoader(BufferStream aBuffer)
	{
		long theLoaderId = aBuffer.getLong();
		long theLoaderClassId = aBuffer.getLong();
		
		// TODO: register
	}
	
	private void processSync(BufferStream aBuffer)
	{
		long theTimestamp = aBuffer.getLong();
		
		// TODO: register
	}
	
	public InScopeReplayerFrame createInScopeFrame(ReplayerFrame aParent, int aBehaviorId)
	{
		throw new UnsupportedOperationException();
	}
	
	public EnveloppeReplayerFrame createEnveloppeFrame(ReplayerFrame aParent)
	{
		throw new UnsupportedOperationException();
	}
	
	public UnmonitoredReplayerFrame createUnmonitoredFrame(ReplayerFrame aParent)
	{
		throw new UnsupportedOperationException();
	}
	
	public ClassloaderWrapperReplayerFrame createClassloaderFrame(ReplayerFrame aParent)
	{
		throw new UnsupportedOperationException();
	}
	
	public IntDeltaReceiver getBehIdReceiver()
	{
		return itsBehIdReceiver;
	}
	
	public TmpIdManager getTmpIdManager()
	{
		return itsTmpIdManager;
	}
	
	public ObjectId readRef()
	{
		byte theType = itsStream.get();
		switch(theType)
		{
		case ValueType.OBJECT_ID: return new ObjectId(itsObjIdReceiver.receiveFull(itsStream));
		case ValueType.OBJECT_ID_DELTA: return new ObjectId(itsObjIdReceiver.receiveDelta(itsStream));
		case ValueType.NULL: return null;
		default: throw new RuntimeException("Not handled: "+theType); 
		}
	}
	
	public ExceptionInfo readExceptionInfo()
	{
		String theMethodName = itsStream.getString();
		String theMethodSignature = itsStream.getString();
		String theDeclaringClassSignature = itsStream.getString();
		short theBytecodeIndex = itsStream.getShort();
		ObjectId theException = readRef();
		
		String theClassName;
		try
		{
			theClassName = Type.getType(theDeclaringClassSignature).getClassName();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Bad declaring class signature: "+theDeclaringClassSignature, e);
		}
		
		int theBehaviorId = getDatabase().getBehaviorId(theClassName, theMethodName, theMethodSignature);

		itsLastException = new ExceptionInfo(
				theMethodName, 
				theMethodSignature, 
				theDeclaringClassSignature,
				theBehaviorId,
				theBytecodeIndex, 
				theException);
		
		return itsLastException;
	}
	
	public ExceptionInfo getLastException()
	{
		return itsLastException;
	}
	
	public static final String REPLAYER_NAME_PREFIX = "$tod$replayer2$";

	/**
	 * Returns the JVM name of the replayer class for the given method.
	 */
	public static String makeReplayerClassName(String aJvmClassName, String aJvmMethodName, String aDesc)
	{
		String theName = aJvmClassName+"_"+aJvmMethodName+"_"+aDesc;
		StringBuilder theBuilder = new StringBuilder(theName.length());
		for (int i=0;i<theName.length();i++)
		{
			char c = theName.charAt(i);
			switch(c)
			{
			case '/':
			case '(':
			case ')':
			case '<':
			case '>':
			case '[':
			case ';':
				c = '_';
				break;
			}
			theBuilder.append(c);
		}
		return REPLAYER_NAME_PREFIX+theBuilder.toString();
	}
	

	
	public static class ExceptionInfo
	{
		public final String methodName;
		public final String methodSignature;
		public final String declaringClassSignature;
		public final int behaviorId;
		public final short bytecodeIndex;
		public final ObjectId exception;
		
		public ExceptionInfo(
				String aMethodName,
				String aMethodSignature,
				String aDeclaringClassSignature,
				int aBehaviorId,
				short aBytecodeIndex,
				ObjectId aException)
		{
			methodName = aMethodName;
			methodSignature = aMethodSignature;
			declaringClassSignature = aDeclaringClassSignature;
			behaviorId = aBehaviorId;
			bytecodeIndex = aBytecodeIndex;
			exception = aException;
		}
	}

}