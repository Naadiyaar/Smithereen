package smithereen.templates;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import smithereen.model.attachments.SizedAttachment;

public class MediaLayoutHelper{
	public static final int MAX_WIDTH=1000;
	public static final int MAX_HEIGHT=1777; // 9:16
	public static final int MIN_HEIGHT=475; // ~2:1
	public static final float GAP=1.5f;

	private MediaLayoutHelper(){}

	@NotNull
	public static TiledLayoutResult makeLayout(List<? extends SizedAttachment> thumbs){
		if(thumbs.size()<2)
			throw new IllegalArgumentException("Minimum attachment count for tiled layout is 2");

		boolean allAreWide=true, allAreSquare=true;
		ArrayList<Float> ratios=new ArrayList<>();
		int cnt=thumbs.size();

		TiledLayoutResult result=new TiledLayoutResult();

		for(SizedAttachment thumb : thumbs){
			float ratio=thumb.isSizeKnown() ? thumb.getWidth()/(float) thumb.getHeight() : 1f;
			if(ratio<=1.2f){
				allAreWide=false;
				if(ratio<0.8f)
					allAreSquare=false;
			}else{
				allAreSquare=false;
			}
			ratios.add(ratio);
		}

		float avgRatio=sum(ratios)/ratios.size();

		float maxRatio=(float) MAX_WIDTH/MAX_HEIGHT;

		if(cnt==2){
			if(allAreWide && avgRatio>1.4*maxRatio && Math.abs(ratios.get(1)-ratios.get(0))<0.2){ // two wide photos, one above the other
				float h=Math.max(Math.min(MAX_WIDTH/ratios.get(0), Math.min(MAX_WIDTH/ratios.get(1), (MAX_HEIGHT-GAP)/2.0f)), MIN_HEIGHT/2f);

				result.width=MAX_WIDTH;
				result.height=Math.round(h*2+GAP);
				result.columnSizes=new int[]{result.width};
				result.rowSizes=new int[]{Math.round(h), Math.round(h)};
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(1, 1, 1f, h/result.height),
						new TiledLayoutResult.Tile(1, 1, 1f, h/result.height)
				};
			}else if(allAreWide){ // two wide photos, one above the other, different ratios
				result.width=MAX_WIDTH;
				float h0=MAX_WIDTH/ratios.get(0);
				float h1=MAX_WIDTH/ratios.get(1);
				if(h0+h1<MIN_HEIGHT){
					float prevTotalHeight=h0+h1;
					h0=MIN_HEIGHT*(h0/prevTotalHeight);
					h1=MIN_HEIGHT*(h1/prevTotalHeight);
				}
				result.height=Math.round(h0+h1+GAP);
				result.rowSizes=new int[]{Math.round(h0), Math.round(h1)};
				result.columnSizes=new int[]{MAX_WIDTH};
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(1, 1, 1f, h0/result.height),
						new TiledLayoutResult.Tile(1, 1, 1f, h1/result.height)
				};
			}else if(allAreSquare){ // next to each other, same ratio
				float w=((MAX_WIDTH-GAP)/2);
				float h=Math.max(Math.min(w/ratios.get(0), Math.min(w/ratios.get(1), MAX_HEIGHT)), MIN_HEIGHT);

				result.width=MAX_WIDTH;
				result.height=Math.round(h);
				result.columnSizes=new int[]{Math.round(w), MAX_WIDTH-Math.round(w)};
				result.rowSizes=new int[]{Math.round(h)};
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(1, 1, w/MAX_WIDTH, 1f),
						new TiledLayoutResult.Tile(1, 1, w/MAX_WIDTH, 1f)
				};
			}else{ // next to each other, different ratios
				float w0=((MAX_WIDTH-GAP)/ratios.get(1)/(1/ratios.get(0)+1/ratios.get(1)));
				float w1=(MAX_WIDTH-w0-GAP);
				float h=Math.max(Math.min(MAX_HEIGHT, Math.min(w0/ratios.get(0), w1/ratios.get(1))), MIN_HEIGHT);

				result.columnSizes=new int[]{Math.round(w0), Math.round(w1)};
				result.rowSizes=new int[]{Math.round(h)};
				result.width=Math.round(w0+w1+GAP);
				result.height=Math.round(h);
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(1, 1, w0/MAX_WIDTH, 1f),
						new TiledLayoutResult.Tile(1, 1, w1/MAX_WIDTH, 1f)
				};
			}
		}else if(cnt==3){
			if((ratios.get(0) > 1.2 * maxRatio || avgRatio > 1.5 * maxRatio) || allAreWide){ // 2nd and 3rd photos are on the next line
				float hCover=Math.min(MAX_WIDTH/ratios.get(0), (MAX_HEIGHT-GAP)*0.66f);
				float w2=((MAX_WIDTH-GAP)/2);
				float h=Math.min(MAX_HEIGHT-hCover-GAP, Math.min(w2/ratios.get(1), w2/ratios.get(2)));
				if(hCover+h<MIN_HEIGHT){
					float prevTotalHeight=hCover+h;
					hCover=MIN_HEIGHT*(hCover/prevTotalHeight);
					h=MIN_HEIGHT*(h/prevTotalHeight);
				}
				result.width=MAX_WIDTH;
				result.height=Math.round(hCover+h+GAP);
				result.columnSizes=new int[]{Math.round(w2), MAX_WIDTH-Math.round(w2)};
				result.rowSizes=new int[]{Math.round(hCover), Math.round(h)};
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(2, 1, 1f, hCover/result.height),
						new TiledLayoutResult.Tile(1, 1, w2/MAX_WIDTH, h/result.height),
						new TiledLayoutResult.Tile(1, 1, 1f-(w2/MAX_WIDTH), h/result.height)
				};
			}else{ // 2nd and 3rd photos are on the right part
				float height=Math.min(MAX_HEIGHT, MAX_WIDTH*0.66f/avgRatio);
				float wCover=Math.min(height*ratios.get(0), (MAX_WIDTH-GAP)*0.66f);
				float h1=(ratios.get(1)*(height-GAP)/(ratios.get(2)+ratios.get(1)));
				float h0=(height-h1-GAP);
				float w=Math.min(MAX_WIDTH-wCover-GAP, Math.min(h1*ratios.get(2), h0*ratios.get(1)));
				result.width=Math.round(wCover+w+GAP);
				result.height=Math.round(height);
				result.columnSizes=new int[]{Math.round(wCover), Math.round(w)};
				result.rowSizes=new int[]{Math.round(h0), Math.round(h1)};
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(1, 2, wCover/MAX_WIDTH, result.height/height),
						new TiledLayoutResult.Tile(1, 1, w/MAX_WIDTH, h0/height),
						new TiledLayoutResult.Tile(1, 1, w/MAX_WIDTH, h1/height)
				};
			}
		}else if(cnt==4){
			if((ratios.get(0) > 1.2 * maxRatio || avgRatio > 1.5 * maxRatio) || allAreWide){ // 2nd, 3rd and 4th photos are on the next line
				float hCover=Math.min(MAX_WIDTH/ratios.get(0), (MAX_HEIGHT-GAP)*0.66f);
				float h=(MAX_WIDTH-2*GAP)/(ratios.get(1)+ratios.get(2)+ratios.get(3));
				float w0=h*ratios.get(1);
				float w1=h*ratios.get(2);
				float w2=h*ratios.get(3);
				h=Math.min(MAX_HEIGHT-hCover-GAP, h);
				if(hCover+h<MIN_HEIGHT){
					float prevTotalHeight=hCover+h;
					hCover=MIN_HEIGHT*(hCover/prevTotalHeight);
					h=MIN_HEIGHT*(h/prevTotalHeight);
				}
				result.width=MAX_WIDTH;
				result.height=Math.round(hCover+h+GAP);
				result.columnSizes=new int[]{Math.round(w0), Math.round(w1), MAX_WIDTH-Math.round(w0)-Math.round(w1)};
				result.rowSizes=new int[]{Math.round(hCover), Math.round(h)};
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(3, 1, 1f, hCover/result.height),
						new TiledLayoutResult.Tile(1, 1, w0/MAX_WIDTH, h/result.height),
						new TiledLayoutResult.Tile(1, 1, w1/MAX_WIDTH, h/result.height),
						new TiledLayoutResult.Tile(1, 1, w2/MAX_WIDTH, h/result.height),
				};
			}else{ // 2nd, 3rd and 4th photos are on the right part
				float height=Math.min(MAX_HEIGHT, MAX_WIDTH*0.66f/avgRatio);
				float wCover= Math.min(height*ratios.get(0), (MAX_WIDTH-GAP)*0.66f);
				float w=(height-2*GAP)/(1/ratios.get(1)+1/ratios.get(2)+1/ratios.get(3));
				float h0=w/ratios.get(1);
				float h1=w/ratios.get(2);
				float h2=w/ratios.get(3)+GAP;
				w=Math.min(MAX_WIDTH-wCover-GAP, w);
				result.width=Math.round(wCover+GAP+w);
				result.height=Math.round(height);
				result.columnSizes=new int[]{Math.round(wCover), Math.round(w)};
				result.rowSizes=new int[]{Math.round(h0), Math.round(h1), Math.round(h2)};
				result.tiles=new TiledLayoutResult.Tile[]{
						new TiledLayoutResult.Tile(1, 3, wCover/result.width, 1f),
						new TiledLayoutResult.Tile(1, 1, w/result.width, h0/height),
						new TiledLayoutResult.Tile(1, 1, w/result.width, h1/height),
						new TiledLayoutResult.Tile(1, 1, w/result.width, h2/height),
				};
			}
		}else{
			ArrayList<Float> ratiosCropped=new ArrayList<>();
			if(avgRatio>1.1){
				for(float ratio : ratios){
					ratiosCropped.add(Math.max(1.0f, ratio));
				}
			}else{
				for(float ratio : ratios){
					ratiosCropped.add(Math.min(1.0f, ratio));
				}
			}

			HashMap<int[], float[]> tries=new HashMap<>();

			// One line
			int firstLine, secondLine;
			tries.put(new int[]{cnt}, new float[]{calculateMultiThumbsHeight(ratiosCropped)});

			// Two lines
			for(firstLine=1; firstLine<=cnt-1; firstLine++){
				tries.put(new int[]{firstLine, cnt-firstLine}, new float[]{
								calculateMultiThumbsHeight(ratiosCropped.subList(0, firstLine)),
								calculateMultiThumbsHeight(ratiosCropped.subList(firstLine, ratiosCropped.size()))
						}
				);
			}

			// Three lines
			for(firstLine=1; firstLine<=cnt-2; firstLine++){
				for(secondLine=1; secondLine<=cnt-firstLine-1; secondLine++){
					tries.put(new int[]{firstLine, secondLine, cnt-firstLine-secondLine}, new float[]{
									calculateMultiThumbsHeight(ratiosCropped.subList(0, firstLine)),
									calculateMultiThumbsHeight(ratiosCropped.subList(firstLine, firstLine+secondLine)),
									calculateMultiThumbsHeight(ratiosCropped.subList(firstLine+secondLine, ratiosCropped.size()))
							}
					);
				}
			}

			// Looking for minimum difference between thumbs block height and maxHeight (may probably be little over)
			final int realMaxHeight=Math.min(MAX_HEIGHT, MAX_WIDTH);
			int[] optConf=null;
			float optDiff=0;
			for(int[] conf : tries.keySet()){
				float[] heights=tries.get(conf);
				float confH=GAP*(heights.length-1);
				for(float h : heights) confH+=h;
				float confDiff=Math.abs(confH-realMaxHeight);
				if(conf.length>1){
					if(conf[0]>conf[1] || conf.length>2 && conf[1]>conf[2]){
						confDiff*=1.1f;
					}
				}
				if(optConf==null || confDiff<optDiff){
					optConf=conf;
					optDiff=confDiff;
				}
			}

			ArrayList<SizedAttachment> thumbsRemain=new ArrayList<>(thumbs);
			ArrayList<Float> ratiosRemain=new ArrayList<>(ratiosCropped);
			float[] optHeights=tries.get(optConf);
			int k=0;

			result.width=MAX_WIDTH;
			result.rowSizes=new int[optHeights.length];
			result.tiles=new TiledLayoutResult.Tile[thumbs.size()];
			float totalHeight=0f;
			ArrayList<Integer> gridLineOffsets=new ArrayList<>();
			ArrayList<ArrayList<TiledLayoutResult.Tile>> rowTiles=new ArrayList<>(optHeights.length);

			for(int i=0; i<optConf.length; i++){
				int lineChunksNum=optConf[i];
				ArrayList<SizedAttachment> lineThumbs=new ArrayList<>();
				for(int j=0; j<lineChunksNum; j++) lineThumbs.add(thumbsRemain.removeFirst());
				float lineHeight=optHeights[i];
				totalHeight+=lineHeight;
				result.rowSizes[i]=Math.round(lineHeight);
				int totalWidth=0;
				ArrayList<TiledLayoutResult.Tile> row=new ArrayList<>();
				for(int j=0; j<lineThumbs.size(); j++){
					float thumb_ratio=ratiosRemain.removeFirst();
					float w=j==lineThumbs.size()-1 ? (MAX_WIDTH-totalWidth) : (thumb_ratio*lineHeight);
					totalWidth+=Math.round(w);
					if(j<lineThumbs.size()-1 && !gridLineOffsets.contains(totalWidth))
						gridLineOffsets.add(totalWidth);
					TiledLayoutResult.Tile tile=new TiledLayoutResult.Tile(1, 1, Math.round(w), lineHeight);
					result.tiles[k]=tile;
					row.add(tile);
					k++;
				}
				rowTiles.add(row);
			}
			Collections.sort(gridLineOffsets);
			gridLineOffsets.add(MAX_WIDTH);
			result.columnSizes=new int[gridLineOffsets.size()];
			result.columnSizes[0]=gridLineOffsets.getFirst();
			for(int i=gridLineOffsets.size()-1; i>0; i--){
				result.columnSizes[i]=gridLineOffsets.get(i)-gridLineOffsets.get(i-1);
			}

			for(ArrayList<TiledLayoutResult.Tile> row : rowTiles){
				int columnOffset=0;
				for(TiledLayoutResult.Tile tile : row){
					int startColumn=columnOffset;
					int width=0;
					tile.colSpan=0;
					for(int i=startColumn; i<result.columnSizes.length; i++){
						width+=result.columnSizes[i];
						tile.colSpan++;
						if(Math.abs(width-tile.width)<GAP){
							break;
						}
					}
					columnOffset+=tile.colSpan;
				}
			}
			result.height=Math.round(totalHeight+GAP*(optHeights.length-1));
			for(TiledLayoutResult.Tile tile:result.tiles){
				tile.width/=result.width;
				tile.height/=result.height;
			}
		}

		return result;
	}

	private static float sum(List<Float> a){
		float sum=0;
		for(float f:a) sum+=f;
		return sum;
	}

	private static float calculateMultiThumbsHeight(List<Float> ratios){
		return ((float) MAX_WIDTH-(ratios.size()-1)*GAP)/sum(ratios);
	}

	public static class TiledLayoutResult{
		public int[] columnSizes, rowSizes; // sizes in grid fractions
		public Tile[] tiles;
		public int width, height; // in pixels (510x510 max)

		@Override
		public String toString(){
			return "TiledLayoutResult{"+
					"columnSizes="+Arrays.toString(columnSizes)+
					", rowSizes="+Arrays.toString(rowSizes)+
					", tiles="+Arrays.toString(tiles)+
					", width="+width+
					", height="+height+
					'}';
		}

		public static class Tile{
			public int colSpan, rowSpan;
			public float width, height;

			public Tile(int colSpan, int rowSpan, float width, float height){
				this.colSpan=colSpan;
				this.rowSpan=rowSpan;
				this.width=width;
				this.height=height;
			}

			@Override
			public String toString(){
				return "Tile{"+
						"colSpan="+colSpan+
						", rowSpan="+rowSpan+
						", width="+width+
						", height="+height+
						'}';
			}
		}
	}
}
