
public class Request {

	int requestId;
	int startLoc;
	int descLoc;
	int requestTime;

	public Request(int requestId, int startLoc, int descLoc, int requestTime) {
		super();
		this.requestId = requestId;
		this.startLoc = startLoc;
		this.descLoc = descLoc;
		this.requestTime = requestTime;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getStartLoc() {
		return startLoc;
	}

	public void setStartLoc(int startLoc) {
		this.startLoc = startLoc;
	}

	public int getDescLoc() {
		return descLoc;
	}

	public void setDescLoc(int descLoc) {
		this.descLoc = descLoc;
	}

	public int getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(int requestTime) {
		this.requestTime = requestTime;
	}

	@Override
	public String toString() {
		return "reqId:" + requestId + ", reqTime:" + requestTime + ", startLoc:" + startLoc
				+ ", descLoc:" + descLoc;
	}

}
