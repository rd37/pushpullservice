package yakit.utils;

public class YaKitUtils {
	private static YaKitUtils utils = new YaKitUtils();
	
	private YaKitUtils(){}
	
	public static YaKitUtils getInstance() {return utils;}
	
	public String getFileName(String fileName){
		int index = fileName.lastIndexOf('/');
		if(index==-1)
			return fileName;
		return fileName.substring(index, fileName.length());
	}
}
