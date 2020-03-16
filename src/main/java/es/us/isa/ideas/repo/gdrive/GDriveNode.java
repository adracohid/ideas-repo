package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.Node;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSNodeIcon;

public class GDriveNode extends Node{

	private String icon;
	
	
	
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}

	
	@Override
	public String toString() {
		String s="{";
		s+="\"icon\":"+getIcon()+",";
		s+="\"isFolder\":"+isFolder()+",";
		s+= "\"children\": [";
		
		for ( int i = 0; i < getChildren().size() ; i++ ) {
			if (  i != 0 ) {
				s += ",";
			}
			GDriveNode child =  (GDriveNode) getChildren().get(i);
			s+= child.toString();
		}
	
	s+= "]";
	s+="}";
	return s;
	}
	
	public static List<Node> getChildren(String id, Drive credentials) throws GeneralSecurityException, IOException {
		List<File> files=new ArrayList<File>(DriveQuickstart.getFilesByFolderId(id,credentials));
		List<Node> result=new ArrayList<>();
		for(File f:files) {			
			FSNode child=new FSNode();
			child.setTitle(f.getName());
			result.add(child);
			if(f.getMimeType().equals("application/vnd.google-apps.folder")) {
				List<Node> children=getChildren(f.getId(),credentials);
				child.setFolder(true);
				child.setIcon(FSNodeIcon.FOLDER);
				child.setChildren(children);
			}
		}
		return result;
	}
}
