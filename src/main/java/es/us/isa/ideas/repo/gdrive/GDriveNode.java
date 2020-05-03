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
import static org.apache.commons.io.FilenameUtils.getExtension;


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
	
	public static List<Node> getChildren(String path,FSNode parentNode,String id, Drive credentials) throws GeneralSecurityException, IOException {
		List<File> files=new ArrayList<File>(DriveQuickstart.getFilesByFolderId(id,credentials));
		List<Node> result=new ArrayList<>();
		
		for(File f:files) {			
			FSNode child=new FSNode();
			child.setTitle(f.getName());
			String newPath=null;
			if(path.equals("")) {
			newPath=child.getTitle();
			}else {
			newPath=path+"/"+child.getTitle();	
			}
			
			child.setKeyPath(newPath);
			child.setType(Node.GOOGLE_DRIVE);
			result.add(child);
			
			if(f.getMimeType().equals("application/vnd.google-apps.folder")) {
				
				child.setFolder(true);
				if(parentNode.getIcon().equals(FSNodeIcon.WORKSPACE)) {
					child.setIcon(FSNodeIcon.PROJECT);
				}else {
				child.setIcon(FSNodeIcon.FOLDER);
				}
				List<Node> children=getChildren(newPath,child,f.getId(),credentials);
				child.setChildren(children);
			}else {
				child.setIcon(FSNodeIcon.iconFor(getExtension(child.getTitle())));
			}
		}
		return result;
	}
}
