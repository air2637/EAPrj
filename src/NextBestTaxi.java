
public class NextBestTaxi {

	boolean past;
	int taxiId;
	int taxiLoc;
	double waitTime;

	public NextBestTaxi(int taxiLoc, int taxiId, double waitTime, boolean past) {
		super();
		this.taxiLoc = taxiLoc;
		this.taxiId = taxiId;
		this.waitTime = waitTime;
		this.past = past;
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

	public boolean isPast() {
		return past;
	}

	public void setPast(boolean past) {
		this.past = past;
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
		return "taxiLoc:" + taxiLoc + ", taxiId:" + taxiId + ", waitTime:" + waitTime;
	}

}
