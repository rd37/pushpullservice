package yakitdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;



public class YaKitDerbyDB {
	private static YaKitDerbyDB database = new YaKitDerbyDB();

	private YaKitDerbyDB(){}

	public static YaKitDerbyDB getInstance(){return database;};

	private Random random;
	/* the default framework is embedded*/
    //private String framework = "embedded";
    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private String protocol = "jdbc:derby:";
    
    private String litToolDB = "YaKitDataBase";
    private Connection conn;
    
    private PreparedStatement appChannelKeyInsert = null;
    private PreparedStatement appChannelURIInsert = null;
    private PreparedStatement appChannelURIRemove = null;
    private PreparedStatement appChannelURIRemovebyKey = null;
    private PreparedStatement appChannelKeyRemove = null;
    
    private LinkedList<Statement> sqlStatements = new LinkedList<Statement>();
    private static boolean initialized=false;
	/*
	 * check if tables exist
	 * if so, then create groups,products,literature review, and reviews
	 * if not,
	 * then done.
	 */
	public void initialize(){
		if(initialized){
			return;
		}else{
			initialized=true;
		}
		/* 
		 * load the desired JDBC driver 
		 * Starts the derby service, but not database
		 * */
        loadDriver();
        try{
        	Properties props = new Properties(); // connection properties
            // providing a user name and password is optional in the embedded
            // and derby client frameworks
            props.put("user", "yakittool");
            props.put("password", "yakittool@929!");
            /*
             * This connection specifies create=true in the connection URL to
             * cause the database to be created when connecting for the first
             * time. To remove the database, remove the directory derbyDB (the
             * same as the database name) and its contents.
             *
             * The directory derbyDB will be created under the directory that
             * the system property derby.system.home points to, or the current
             * directory (user.dir) if derby.system.home is not set.
             */
            conn = DriverManager.getConnection(protocol + litToolDB
                    + ";create=true", props);

            System.out.println("Connected to and created database " + litToolDB);

            // We want to control transactions manually. Autocommit is on by
            // default in JDBC.
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            //Statement s1 = conn.createStatement();
            try{
            	s.execute("create table applicationchannels( id int primary key,  appchannelkey int ) ");
            	s.execute("create table applicationchannelfiles( id int primary key,  appchannelkey int, machineip varchar(20), type varchar(100), fileuri varchar(256) ) ");
             }catch(Exception e){
            	// e.printStackTrace();
            	System.out.println("Tables created, carry on");
            }
            sqlStatements.add(s);//sqlStatements.add(s1);
            
            appChannelKeyInsert = conn.prepareStatement("insert into applicationchannels values (? , ?)");
            appChannelKeyRemove = conn.prepareStatement("delete from applicationchannels where appchannelkey=?");
            appChannelURIInsert = conn.prepareStatement("insert into applicationchannelfiles values ( ? , ? , ? , ? , ? )");
            appChannelURIRemove = conn.prepareStatement("delete from applicationchannelfiles where id=?");
            appChannelURIRemovebyKey = conn.prepareStatement("delete from applicationchannelfiles where appchannelkey=?");
        }catch(Exception e){
        	System.out.println("Unable to initialize database "+e);
        	e.printStackTrace();
        }
        random=new Random(456);
	}
	
	public void pushFileLocation(int appKey, String machineIp, String type, String fileLocation){
		try{
			
			appChannelURIInsert.setInt(1, random.nextInt() );
			appChannelURIInsert.setInt(2, appKey);
			appChannelURIInsert.setString(3, machineIp);
			appChannelURIInsert.setString(4, type);
			appChannelURIInsert.setString(5, fileLocation);
			appChannelURIInsert.executeUpdate();
			conn.commit();
			
		}catch(Exception e){
			System.out.println("Error:"+e);
		}
	}
	
	public String popFileLocation(int appKey){
		    String fileLocation=null;
		    int id;
			/*
			 * 1. get first result of querying with app key
			 * 2. use result and remove entry from talbe
			 * 3. return result
			 */
			ResultSet urilocations;
			try {
				//System.out.println("Try to find row with appkey ok "+appKey);
				//System.out.println("sql:: select * from applicationchannelfiles where appchannelkey="+appKey);
				urilocations = this.sqlStatements.get(0).executeQuery("select * from applicationchannelfiles where appchannelkey="+appKey);
				//System.out.println("Result set acheived");
				//urilocations.getStr
				urilocations.next();
				fileLocation=urilocations.getString(5);
				id=urilocations.getInt(1);
				if(fileLocation!=null){
					//System.out.println("found file location "+fileLocation);
					//appChannelURIRemove.setString(1, fileLocation);
					appChannelURIRemove.setInt(1, id);
					appChannelURIRemove.executeUpdate();
					conn.commit();
					//System.out.println("Found file "+fileLocation+" using key "+appKey);
					return fileLocation;
				}else{
					System.out.println("File is Null for some reason?");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Error finding first file location with "+appKey+" either key is no longer valide or no files are in system");
			return null;
	}
	
	public void insertAppKey(int appKey){
		try{
			
			appChannelKeyInsert.setInt(1, random.nextInt());
			appChannelKeyInsert.setInt(2, appKey);
			appChannelKeyInsert.executeUpdate();
			conn.commit();
			
		}catch(Exception e){
			System.out.println("Error:"+e);
		}
		
	}
	
	public void removeAppKey(int appKey){
		
		try{
			appChannelKeyRemove.setInt(1, appKey);
			appChannelKeyRemove.executeUpdate();
			appChannelURIRemovebyKey.setInt(1, appKey);
			appChannelURIRemovebyKey.executeUpdate();
			conn.commit();
		}catch(Exception e){
			System.out.println("Error removing id "+appKey);
			e.printStackTrace();
		}
	}

	public int getKey(){
		return random.nextInt();
	}
	
	

	private void loadDriver() {
        try {
        	System.out.println("Try to load driver "+driver);
            Class.forName(driver).newInstance();
            System.out.println("Loaded the appropriate driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("\nUnable to load the JDBC driver " + driver);
            System.err.println("Please check your CLASSPATH.");
            cnfe.printStackTrace(System.err);
        } catch (InstantiationException ie) {
            System.err.println(
                        "\nUnable to instantiate the JDBC driver " + driver);
            ie.printStackTrace(System.err);
        } catch (IllegalAccessException iae) {
            System.err.println(
                        "\nNot allowed to access the JDBC driver " + driver);
            iae.printStackTrace(System.err);
        }
    }

	public void showTable(String type){
		try{
			if(type.equals("applicationchannels")){
				ResultSet set = this.sqlStatements.get(0).executeQuery("select * from "+type);
				System.out.println("****************"+type+"****************");
				System.out.println("| id \t\t| appid \t|  ");
				while(set.next()){ 
					System.out.println("|"+set.getInt(1)+"\t|"+set.getInt(2)+"\t|" );
				}
				System.out.println("**************************************");
			}else if(type.equals("applicationchannelfiles")){
				ResultSet set = this.sqlStatements.get(0).executeQuery("select * from "+type);
				System.out.println("****************"+type+"****************");
				System.out.println("| id \t\t| appid \t| machine ip \t| content type \t| file uri \t| ");
				while(set.next()){ 
					System.out.println("|"+set.getInt(1)+"\t|"+set.getInt(2)+"\t|"+set.getString(3)+"\t|"+set.getString(4)+"\t|"+set.getString(5)+"\t|");
				}
				System.out.println("**************************************");
			}
			
			//System.out.println("|"+set.getInt(1)+"\t|"+set.getInt(2)+"\t|"+set.getString(3)+"\t|"+set.getInt(4)+"\t|");
		}catch(Exception e){
			System.out.println("error showing talbe "+e);
			e.printStackTrace();
		}
	}
}