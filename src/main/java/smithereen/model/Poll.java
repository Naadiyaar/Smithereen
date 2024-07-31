package smithereen.model;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import smithereen.storage.DatabaseUtils;

public class Poll implements OwnedContentObject{
	public int id;
	public int ownerID;
	public String question;
	public boolean multipleChoice;
	public boolean anonymous;
	public List<PollOption> options;
	public Instant endTime;
	public int numVoters;
	public URI activityPubID;
	public Instant lastVoteTime;

	public static Poll fromResultSet(ResultSet res) throws SQLException{
		Poll p=new Poll();
		p.id=res.getInt("id");
		p.ownerID=res.getInt("owner_id");
		p.question=res.getString("question");
		p.multipleChoice=res.getBoolean("is_multi_choice");
		p.anonymous=res.getBoolean("is_anonymous");
		p.endTime=DatabaseUtils.getInstant(res, "end_time");
		p.numVoters=res.getInt("num_voted_users");
		p.lastVoteTime=DatabaseUtils.getInstant(res, "last_vote_time");
		p.options=new ArrayList<>();
		String apID=res.getString("ap_id");
		if(apID!=null)
			p.activityPubID=URI.create(apID);
		return p;
	}

	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder("Poll{");
		sb.append("id=");
		sb.append(id);
		sb.append(", ownerID=");
		sb.append(ownerID);
		if(question!=null){
			sb.append(", question='");
			sb.append(question);
			sb.append('\'');
		}
		sb.append(", multipleChoice=");
		sb.append(multipleChoice);
		sb.append(", anonymous=");
		sb.append(anonymous);
		if(options!=null){
			sb.append(", options=");
			sb.append(options);
		}
		if(endTime!=null){
			sb.append(", endTime=");
			sb.append(endTime);
		}
		sb.append(", numVoters=");
		sb.append(numVoters);
		sb.append('}');
		return sb.toString();
	}

	public boolean isExpired(){
		return endTime!=null && endTime.toEpochMilli()<System.currentTimeMillis();
	}

	@Override
	public boolean equals(Object _other){
		if(!(_other instanceof Poll other))
			return false;
		if(!Objects.equals(question, other.question) || multipleChoice!=other.multipleChoice || anonymous!=other.anonymous
			|| !Objects.equals(endTime, other.endTime) || options.size()!=other.options.size())
			return false;
		for(int i=0;i<options.size();i++){
			if(!Objects.equals(options.get(i).text, other.options.get(i).text))
				return false;
		}
		return true;
	}

	@Override
	public int getOwnerID(){
		return ownerID;
	}

	@Override
	public int getAuthorID(){
		return 0;
	}

	@Override
	public long getObjectID(){
		return id;
	}
}
