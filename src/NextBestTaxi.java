
public class NextBestTaxi {

	int taxiLoc;
	int taxiId;
	double waitTime;
	boolean past;

	public int getTaxiLoc() {
		return taxiLoc;
	}

	public void setTaxiLoc(int taxiLoc) {
		this.taxiLoc = taxiLoc;
	}

	public int getTaxiId() {
		return taxiId;
	}

	public void setTaxiId(int taxiId) {
		this.taxiId = taxiId;
	}

	public double getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(double waitTime) {
		this.waitTime = waitTime;
	}

	public NextBestTaxi(int taxiLoc, int taxiId, double waitTime, boolean past) {
		super();
		this.taxiLoc = taxiLoc;
		this.taxiId = taxiId;
		this.waitTime = waitTime;
		this.past = past;
	}

	public boolean isPast() {
		return past;
	}

	public void setPast(boolean past) {
		this.past = past;
	}

	@Override
	public String toString() {
		return "taxiLoc:" + taxiLoc + ", taxiId:" + taxiId + ", waitTime:" + waitTime;
	}

}
