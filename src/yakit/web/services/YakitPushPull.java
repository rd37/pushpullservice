package yakit.web.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import yakit.utils.YaKitUtils;
import yakitdb.YaKitDerbyDB;

/**
 * Servlet implementation class YakitPushPull
 */
public class YakitPushPull extends HttpServlet {
	private static final long serialVersionUID = 1L;
    //private String filedir = "/Users/ronaldjosephdesmarais/apachefiles/";
    private String filedir = "";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public YakitPushPull() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		YaKitDerbyDB database = YaKitDerbyDB.getInstance();
		database.initialize();
		String operation = request.getParameter("operation");
		if(operation.equals("createnewkey")){
			int newKey = database.getKey();
			database.insertAppKey(newKey);
			response.getWriter().write(""+newKey);
			database.showTable("applicationchannels");
			response.getWriter().close();
		}else if(operation.equals("getfile")){
			/*
			 * 1. get the app key from request
			 * 2. get the uri or file location
			 * 3. write the bytes to the response stream
			 */
			
			int appKey = (new Integer(request.getParameter("appkey"))).intValue();
			String fileLoc = database.popFileLocation(appKey);
			database.showTable("applicationchannelfiles");
			
			if(fileLoc!=null){
				File file = new File(fileLoc);
				FileInputStream fis = new FileInputStream(file);
				
				byte[] buffer = new byte[512];
				int bytesRead = fis.read(buffer);
				
			    while(bytesRead>0){
			    	response.getOutputStream().write(buffer,0, bytesRead);
			    	bytesRead = fis.read(buffer);
			    }
			    fis.close();
			    response.getOutputStream().close();
			    file.delete();
			}else{
				System.out.println("no file found with appkey:"+appKey);
			}
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Do Post");
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		YaKitDerbyDB database = YaKitDerbyDB.getInstance();
		database.initialize();
		String contentType = request.getHeader("Content-Type");
		String contentName = request.getHeader("FileName");
		String contentNameFix=YaKitUtils.getInstance().getFileName(contentName);
		int appKey = (new Integer(request.getHeader("AppKey"))).intValue();
		
		/*
		 * 1. Get file name from header
		 * 2. create the file
		 * 3. write the bytes
		 */
		InputStream is = request.getInputStream();
		FileOutputStream fos = new FileOutputStream(filedir+contentNameFix);
		byte[] buffer = new byte[512];
		int bytesRead = is.read( buffer ) ;
		int offset=0;
		while(bytesRead>0){
			fos.write(buffer,0, bytesRead);
	    	offset+=bytesRead;
			bytesRead = is.read( buffer ) ;
		}
		fos.close();
		is.close();
		
		database.pushFileLocation(appKey,request.getRemoteAddr(), contentType, filedir+contentNameFix);
		database.showTable("applicationchannelfiles");
		response.getWriter().write("File "+contentName+" successfully pushed to Push/Pop Service wrote "+offset+" bytes\n");
		response.getWriter().close();
	}

}
