package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.Directory;
import es.us.isa.ideas.repo.Project;
import es.us.isa.ideas.repo.Repo;
import es.us.isa.ideas.repo.RepoElement;
import es.us.isa.ideas.repo.Workspace;

public class GDriveRepo extends Repo {

	
	private static final long serialVersionUID = -1373826576272704830L;
	private Drive credentials;
	public GDriveRepo(Drive credentials) {
		super();
		this.credentials=credentials;		
	}
	
	public void setCredentials(Drive credentials) {
		this.credentials = credentials;
	}
	
	public String getWorkspaces(String owner, Drive credentials) {
		String res=null;
		try {
			File repoFolder=DriveQuickstart.getRepoFolder(owner, credentials);
			List<File> folders=new ArrayList<>(DriveQuickstart.getFoldersByFolderId(repoFolder.getId(),credentials));
			List<String> workspaces=new ArrayList<>();
			for(File f:folders) {
			//	String ws="{\"name\": \"" + f.getName() + "\"}";
				String ws="{\"name\": \"" + f.getName() + "\","+
			"\"type\":"+"\"Google_Drive"  + "\"}";
				workspaces.add(ws);
			}
			res=workspaces.toString();
		} catch (IOException | GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	@Override
	public String getRepoUri() {
		return getRepoUri("");
		
	}
	//TODO
	//ideas-repo/nombreusuario
	public String getRepoUri(String owner) {
		String res=null;
		if(owner!=null) {
			res="ideas-repo\\"+owner+"\\";
		}else {
			res="ideas-repo\\";
		}
		return res;
	}

	@Override
	public RepoElement generate(Class<? extends RepoElement> elementClass, String[] params) {
		RepoElement result=null;
		if (elementClass.equals(es.us.isa.ideas.repo.File.class) && params.length==4) {
			result= new GDriveFile(params[0],params[1],params[2],params[3],credentials);
		}else if (elementClass.equals(Directory.class) && params.length==4) {
			result = new GDriveDirectory(params[0],params[1],params[2],params[3],credentials);
		}else if(elementClass.equals(Project.class) && params.length==3) {
			result = new GDriveProject(params[0],params[1],params[2],credentials) ;
		}else if(elementClass.equals(Workspace.class))
			result = new GDriveWorkspace(params[0], params[1],credentials);
		return result;
	}
	}


