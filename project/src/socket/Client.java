package socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

	public static void main(String[] args) throws Exception{
		int port = 12000;
		String host = "127.0.0.1";
		Socket socket = new Socket(host,port);
		
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
		
		Scanner scan = new Scanner(System.in);
		pw.println("connect");
		pw.flush();
		
		String ans= br.readLine();
		System.out.println(ans);
		
		while(!ans.equals("登陆成功")) {
			
			ans= br.readLine();
			System.out.println(ans);
			
			String user = scan.nextLine();
			pw.println(user);
			pw.flush();
			
			ans= br.readLine();
			System.out.println(ans);
			
			String pass = scan.nextLine();
			pw.println(pass);
			pw.flush();
			
			ans= br.readLine();
			System.out.println(ans);
		}
		
		while(true) {
			System.out.print("请输入要发送的信息：");
			String str = scan.nextLine();
			pw.println(str);	
			pw.flush();
			
			Pattern pt = Pattern.compile("send.+");
			Matcher mt = pt.matcher(str);
			while(mt.find()) {
				String st = mt.group();
				st = st.replaceAll("send ","");
				send(st,os);
			}
			
			String aws = br.readLine();
			Pattern ptt = Pattern.compile("send.+");
			Matcher mtt = pt.matcher(aws);
			if(mtt.find()) {
				receive(is);
			}
			else
			System.out.println("回复："+aws);
			
			
			if(str.equals("bye"))
				break;
		}
		socket.close();
		System.out.println("客户端程序结束。");

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
			System.out.println("接收成功");
		}

	

}
