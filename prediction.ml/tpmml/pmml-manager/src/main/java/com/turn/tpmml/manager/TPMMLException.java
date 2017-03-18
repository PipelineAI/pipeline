package com.turn.tpmml.manager;

public class TPMMLException extends Exception {

	private static final long serialVersionUID = 1L;
	protected TPMMLCause cause;

	public TPMMLException(String message) {
		super(message);
		cause = TPMMLCause.GENERAL;
	}
	
	public TPMMLException(TPMMLCause c, Object... args) {
		super(String.format(c.getMessage(), args));
		cause = c;
	}
	
	public TPMMLException(Throwable e) {
		super(e);
	}

	public TPMMLCause getTPMMLCause() {
		return cause;
	}


	public enum TPMMLCause {
		GENERAL (1, "%s"),
		MISSING_PARAMETER(2, "The parameter [%s] is missing"),
		UNSUPPORTED_OPERATION(3, "The operation [%s] is not supported");
		
		private int id;
		private String message;

		TPMMLCause(int id, String message) {
			this.id = id;
			this.message = message;
		}
		
		String getMessage() {
			return message;
		}

		public static TPMMLCause fromId(int id) {
			TPMMLCause result = null;
			for (TPMMLCause opType : TPMMLCause.values()) {
				if (opType.id == id) {
					result = opType;
					break;
				}
			}
			return result;
		}
		
	}
}
