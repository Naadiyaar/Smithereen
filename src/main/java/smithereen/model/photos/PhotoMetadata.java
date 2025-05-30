package smithereen.model.photos;

import com.google.gson.annotations.SerializedName;

import java.net.URI;

import smithereen.model.SizedImage;

public class PhotoMetadata{
	@SerializedName("apUrl")
	public URI apURL;
	public URI apReplies;
	public AvatarCropRects cropRects;
	public SizedImage.Rotation rotation;
	public int width, height;
	public String blurhash;
	/**
	 * For user avatars, the ID of the AVATAR_UPDATE post for this avatar
	 */
	@SerializedName("post")
	public int correspondingPostID;
}
