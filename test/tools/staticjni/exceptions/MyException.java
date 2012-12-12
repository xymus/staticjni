package exceptions;

public class MyException extends Exception {

	private static final long serialVersionUID = 620676343787817836L;
	
	String message;
	
	MyException( String message ) {
		this.message = message;
	}

	public MyException()
	{
		super();             // call superclass constructor
		message = "unknown";
	}
}
