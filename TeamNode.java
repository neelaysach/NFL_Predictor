package winLossPredictor;

public class TeamNode {
	protected int ID;
	protected String team;
	protected String teamName;
	protected String oppTeam = null;
	protected boolean isHome;
	
	public TeamNode(int id, String team, String teamName) {
		ID = id;
		this.team = team;
		this.teamName = teamName;
	}
}