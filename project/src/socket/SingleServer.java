package socket;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SingleServer implements Runnable{
	private Socket socket;
	private int clientNo;
	
	public SingleServer(Socket socket, int clientNo) {
		this.socket = socket;
		this.clientNo = clientNo;
	}
	
	public void run() {
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
			
			String tt = br.readLine();
			System.out.println(tt);
			
			pw.println("连接服务器成功！\n请输入用户名:");
			pw.flush();
			String userName = br.readLine();
			System.out.println(userName);
			
			pw.println("请输入密码:");
			pw.flush();
			String password = br.readLine();
			System.out.println(password);
			
			while(!login(userName,password)) {				
				pw.println("用户名或密码错误！\n请重新输入用户名:");
				pw.flush();
				userName = br.readLine();
				
				pw.println("请输入密码:");
				pw.flush();
				password = br.readLine();
			}
			
			pw.println("登陆成功");
			pw.flush();
			
			while(true) {
				String Str = br.readLine(); 
				
				Pattern pt = Pattern.compile("send.+");
				Matcher mt = pt.matcher(Str);
				if(mt.find()) {
					receive(is);
					System.out.print("接收成功");
					pw.println("接收成功");
					pw.flush();
				}else {
					
				System.out.println("客户端来信："+Str);
				
				if(Str.equalsIgnoreCase("bye")) {
					pw.println("bye");
					pw.flush();
					break;
				}
				
				String aws = answer();
				
				if(aws.equalsIgnoreCase("#回复超时#")) {
					String INFO = URLEncoder.encode(Str, "utf-8"); 
					String getURL = "http://www.tuling123.com/openapi/api?key=1c99470a8a8354e248a4c229234d14af"+ "&info=" + INFO;
					URL getUrl = new URL(getURL);
					HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection(); 
					connection.connect(); 
					BufferedReader reader = new BufferedReader(new InputStreamReader( connection.getInputStream(), "utf-8")); 
					String line = reader.readLine(); 
		        
					JsonParser parser = new JsonParser();
					JsonElement element = parser.parse(line);
					if (element.isJsonObject()) {  
						JsonObject object = element.getAsJsonObject();  // 转化为对象
						pw.println("自动回复："+object.get("text").getAsString());
						pw.flush();
					}
					reader.close(); 
				}
				else {
					pw.println(aws);
					pw.flush();
					
					Pattern ptt = Pattern.compile("send.+");
					Matcher mtt = pt.matcher(aws);
					if(mtt.find()) {
						String st = mtt.group();
						st = st.replaceAll("send ","");
						send(st,os);
					}
				}
				}
				
				
			};
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			System.out.println("与客户端" + clientNo + "通信结束");
			try {
				socket.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean login(String userName, String password) throws Exception {
		Scanner scan = new Scanner(System.in);
		Class.forName("com.mysql.jdbc.Driver");
	
		Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false"
				,"root"
				,"000000");	
		//连接的数据库
		String sql = "SELECT * FROM user WHERE username = ? AND password = ?";		//数据库查询语句
		PreparedStatement pst = con.prepareStatement(sql);
		pst.setString(1, userName);
		pst.setString(2, password);
		ResultSet rs = pst.executeQuery();
		boolean res = rs.next();
		con.close();
		
		return res;
	}
	
	public static String answer() throws InterruptedException,ExecutionException {
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        
        Callable<String> call = new Callable<String>() {
            public String call() throws Exception {
            	Scanner scan = new Scanner(System.in);
            	System.out.print("您的回复：");
                String ans = scan.nextLine();
                return ans;
            }
        };
        try {
            Future<String> future = exec.submit(call);
            String obj = future.get(1000 * 30, TimeUnit.MILLISECONDS); //任务处理超时时间设为 30 秒
            return obj;
        } catch (TimeoutException ex) {
            System.out.println("\n回复超时");
            return "#回复超时#";
        } catch (Exception e) {
            System.out.println("回复失败");
            return "#回复失败#";
        }
        // 关闭线程池
       
    }
	
	 public static void send(String str, OutputStream os) throws IOException {
		  
		  FileInputStream fis = new FileInputStream(str);
		  byte[] bytes = new byte[1024];
		  int data;
		  while((data = fis.read(bytes))!= -1){
			  os.write(bytes, 0, data);
		  }
		  fis.close();
		  System.out.println("发送成功!");
		  }

	public static void receive(InputStream is) throws Exception {
		Scanner scan = new Scanner(System.in);
		System.out.print("请输入保存路径：");
		String path = scan.nextLine();
		
		FileOutputStream fos = new FileOutputStream(path);
		byte[] bytes = new byte[1024];
		int data;
		while((data = is.read(bytes))!=-1){
			fos.write(bytes, 0, data);
		}
		fos.close();
		
	}

}