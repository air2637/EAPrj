import java.util.ArrayList;

public class Assignment {

	int descLoc;
	ArrayList<NextBestTaxi> nextBestTaxi;
	int requestId;
	int requestTime;
	int startLoc;
	int taxiId;
	int taxiLoc;
	double waitTime;

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

	public Assignment(int taxiId, int taxiLoc, int requestId, int requestTime, int startLoc,
			int descLoc, ArrayList<NextBestTaxi> nextBestTaxi, double waitTime) {
		super();
		this.taxiId = taxiId;
		this.taxiLoc = taxiLoc;
		this.requestId = requestId;
		this.requestTime = requestTime;
		this.startLoc = startLoc;
		this.waitTime = waitTime;
		this.descLoc = descLoc;
		this.nextBestTaxi = nextBestTaxi;

	}

	public int getDescLoc() {
		return descLoc;
	}

	public ArrayList<NextBestTaxi> getNextBestTaxi() {
		return nextBestTaxi;
	}

	public int getRequestId() {
		return requestId;
	}

	public int getRequestTime() {
		return requestTime;
	}

	public int getStartLoc() {
		return startLoc;
	}

	public int getTaxiId() {
		return taxiId;
	}

	public int getTaxiLoc() {
		return taxiLoc;
	}

	public double getWaitTime() {
		return waitTime;
	}

	public void setDescLoc(int descLoc) {
		this.descLoc = descLoc;
	}

	public void setNextBestTaxi(ArrayList<NextBestTaxi> nextBestTaxi) {
		this.nextBestTaxi = nextBestTaxi;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public void setRequestTime(int requestTime) {
		this.requestTime = requestTime;
	}

	public void setStartLoc(int startLoc) {
		this.startLoc = startLoc;
	}

	public void setTaxiId(int taxiId) {
		this.taxiId = taxiId;
	}

	public void setTaxiLoc(int taxiLoc) {
		this.taxiLoc = taxiLoc;
	}

	public void setWaitTime(double waitTime) {
		this.waitTime = waitTime;
	}

	@Override
	public String toString() {
		return "taxi:" + taxiId + ", taxiLoc:" + taxiLoc + ", reqId:" + requestId + ", reqTime:"
				+ requestTime + ", startLoc:" + startLoc + ", descLoc:" + descLoc;
	}
}
