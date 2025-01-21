package smithereen.activitypub;

import java.net.URI;
import java.util.Collection;

import smithereen.ApplicationContext;
import smithereen.activitypub.objects.Actor;
import smithereen.model.Server;

public class ActivityHandlerContext{
	private String origRequestBody;
	public final Actor ldSignatureOwner;
	public final Actor httpSignatureOwner;
	public final ApplicationContext appContext;

	public ActivityHandlerContext(ApplicationContext appContext, String origRequestBody, Actor ldSignatureOwner, Actor httpSignatureOwner){
		this.appContext=appContext;
		this.origRequestBody=origRequestBody;
		this.ldSignatureOwner=ldSignatureOwner;
		this.httpSignatureOwner=httpSignatureOwner;
	}

	public void forwardActivity(Collection<URI> inboxes, Actor signer){
		forwardActivity(inboxes, signer, null);
	}

	public void forwardActivity(Collection<URI> inboxes, Actor signer, Server.Feature requiredServerFeature){
		if(ldSignatureOwner==null)
			throw new IllegalStateException("Can't forward an activity without LD-signature");
		appContext.getActivityPubWorker().forwardActivity(origRequestBody, signer, inboxes, ldSignatureOwner.activityPubID.getHost(), requiredServerFeature);
	}
}
