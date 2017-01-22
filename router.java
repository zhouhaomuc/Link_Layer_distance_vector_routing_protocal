import java.io.*;
import java.net.*;

class router extends Thread
{
	//get to argument as port and filename
	public static void main(String args[]) throws Exception{
		if(args.length!=2){
			System.out.println("Please enter two arguments as \"port\" and \"file name!\"");
			return;
		}
		//get port and file name
		int port=Integer.parseInt(args[0]);
		String fileName=args[1];
		/*read file for the first time to get the basic information of the neighbor!
 		 *info[][]is used to store information of file, like an table
 		 *recieveArr[][] is array that store information of neighbors
		 * match is direct cost of two links 
		 * sendInfo is string to be sent out
		 * checkInfo is to be restore information of neighbors
		 * */
		String info[][]=new String[2][50];
		String recieveArr[][]=new String[2][50];
		String match=null;
		String sendInfo="";
		String checkInfo="";
		int flag=0;
		int recFlag=0;
		int count=0;
		//construct UDP connection
		DatagramSocket serverSocket = new DatagramSocket(port);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		File file = new File(args[1]);
        	if (file.isFile() && file.exists()){
			//if file exist, open and read it line by line   
			InputStreamReader read = new InputStreamReader(new FileInputStream(file));   
			BufferedReader bufferedReader = new BufferedReader(read);
			String line=null;
			String firstLine = bufferedReader.readLine();
			//sotre file in info[][],as the information table
			//and get sendInfo
			while((line = bufferedReader.readLine()) != null) {
				info[0][flag]=line.substring(0,line.indexOf(" "));
				info[1][flag]=line.substring(line.indexOf(" ")+1);
				sendInfo=sendInfo+line+"\n";
				flag++;
			}
			//send infomation to neighbors
		        for(int i=0;i<flag;i++){
		        	InetAddress neighIP = InetAddress.getByName(info[0][i]);
		        	send(serverSocket,neighIP,sendInfo,port);
		        }
		read.close();
		bufferedReader.close();
			        
        	}else{
        		System.out.print("file not exist");
        	}
		
		while(true){
			sleep(3000);
			//re-read file every time the while is run
			File checkfile=new File(args[1]);
			File newCheck=new File(args[1]);
			if(checkfile.isFile()&&checkfile.exists()){
				//re-read like the first time of the program
				checkInfo="";
				InputStreamReader read=new InputStreamReader(new FileInputStream(checkfile));
				BufferedReader bufferedReader=new BufferedReader(read);
				String line=null;
				String newLine=null;
				String firstLine=bufferedReader.readLine();
				//get checkInfo, to compare with the sendinfo
				while((line=bufferedReader.readLine())!=null){
					checkInfo=checkInfo+line+"\n";
				}
				if(checkInfo.equals(sendInfo)){
				}else{
					InputStreamReader newread=new InputStreamReader(new FileInputStream(newCheck));
					BufferedReader newbufferedReader=new BufferedReader(newread);	
					System.out.println(" sendInfo:"+sendInfo);
					System.out.println("checkInfo:"+checkInfo);	
					sendInfo=checkInfo;
					firstLine=newbufferedReader.readLine();
					while((newLine=newbufferedReader.readLine())!=null){
						System.out.println("line:"+newLine);
						String hostName=newLine.substring(0,newLine.indexOf(" "));
						System.out.println("host:"+hostName);
						String cost=newLine.substring(newLine.indexOf(" ")+1);
						System.out.println("cost:"+cost);
						for(int i=0;i<flag;i++){
							if(hostName.equals(info[0][i])){
								info[1][i]=cost;
								System.out.println(info[0][i]+info[1][i]);
							}else{
								continue;
							}
						}

					}
					System.out.println("11:"+info[0][2]+info[1][2]);
				read.close();
				bufferedReader.close();
				newread.close();
				newbufferedReader.close();
				}
			}else{
				System.out.println("file not exist!");
			}
			for(int i=0;i<flag;i++){
				InetAddress neighIP=InetAddress.getByName(info[0][i]);
				send(serverSocket,neighIP,sendInfo,port);
			}
			DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			/*get infomation from different neighbor! 
			 * predefine the infomaiton format just like data in the file, using "enter"
			 * */
			String sentence = new String( receivePacket.getData());
			String IP = receivePacket.getAddress().getHostName();
			String localhost=null;
			try {
				//get localhost name,to determine which line is not needed
				localhost = InetAddress.getLocalHost().getHostName();  
			} catch (UnknownHostException e) {  
				e.printStackTrace();
			}
			String line=null;
			String recieveInfo[] = sentence.split("\n");
			recFlag=recieveInfo.length;
			//get information from neighbors, store in recieveArr[][]
			for(int i=0;i<recFlag-1;i++){
				String hostname=recieveInfo[i].substring(0,recieveInfo[i].indexOf(" "));
				String value=recieveInfo[i].substring(recieveInfo[i].indexOf(" ")+1);
				recieveArr[0][i]=hostname;
				recieveArr[1][i]=value;
			}
			for(int i=0;i<flag;i++){
				if(IP.equals(info[0][i])){
					match=info[1][i];
				}
			}
			//compare recieveArr to info[][], get a shortest path
			int exist=0;
			for(int i=0;i<recFlag-1;i++){
				exist=0;
				for(int j=0;j<flag;j++){
					if(recieveArr[0][i].equals(localhost)){
						exist=1;
						break;
					}
					if(recieveArr[0][i].equals(info[0][j])){
						if((Double.parseDouble(match)+Double.parseDouble(recieveArr[1][i]))<Double.parseDouble(info[1][j])){
							info[1][j]=Double.toString(Double.parseDouble(match)+Double.parseDouble(recieveArr[1][i]));
							exist=1;
							break;
						}else{
							exist=1;
						}
					}
				}
				if(exist==0){
					flag++;
					info[0][flag-1]=recieveArr[0][i];
					info[1][flag-1]=Double.toString(Double.parseDouble(recieveArr[1][i])+Double.parseDouble(match));
				}
			}
			/*
			 * printout route information in the array
			 * */
			count++;
			System.out.println("## sequence number: "+count);
			for(int i=0;i<flag;i++){
				System.out.println("the shortest route to "+info[0][i]+" is "+info[1][i]);
			}
		}
	}
	//function send, to send information to neighbors
	public static void send(DatagramSocket serverSocket,InetAddress IPAddress,String sendInfo,int port) throws IOException{
		byte[] sendData;
		sendData = sendInfo.getBytes();
		DatagramPacket sendPacket =new DatagramPacket(sendData, sendData.length, IPAddress, port);
		serverSocket.send(sendPacket);
	}
}
