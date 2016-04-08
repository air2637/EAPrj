
public class Assignment {

	int taxiId;
	int taxiLoc;
	int requestId;
	int requestTime;

	public int getTaxiId() {
		return taxiId;
	}

	public void setTaxiId(int taxiId) {
		this.taxiId = taxiId;
	}

	public int getTaxiLoc() {
		return taxiLoc;
	}

	public void setTaxiLoc(int taxiLoc) {
		this.taxiLoc = taxiLoc;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(int requestTime) {
		this.requestTime = requestTime;
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

	int startLoc;
	int descLoc;

	public Assignment(int taxiId, int taxiLoc, int requestId, int requestTime, int startLoc,
			int descLoc) {
		super();
		this.taxiId = taxiId;
		this.taxiLoc = taxiLoc;
		this.requestId = requestId;
		this.requestTime = requestTime;
		this.startLoc = startLoc;
		this.descLoc = descLoc;
	}

	@Override
	public String toString() {
		return "taxi:" + taxiId + ", taxiLoc:" + taxiLoc + ", reqId:" + requestId + ", reqTime:"
				+ requestTime + ", startLoc:" + startLoc + ", descLoc:" + descLoc;
	}
}
