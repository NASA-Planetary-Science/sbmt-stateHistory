package edu.jhuapl.sbmt.stateHistory.model.io;

/**
 * Exception thrown when performing I/O operations on
 * a State History segment
 * @author steelrj1
 *
 */
public class StateHistoryIOException extends Exception
{

	/**
	 *
	 */
	public StateHistoryIOException()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public StateHistoryIOException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public StateHistoryIOException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StateHistoryIOException(String message, Throwable cause)
	{
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public StateHistoryIOException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
