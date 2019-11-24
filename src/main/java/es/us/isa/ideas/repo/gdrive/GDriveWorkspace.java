package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import es.us.isa.ideas.repo.Node;
import es.us.isa.ideas.repo.Workspace;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSNodeIcon;
import es.us.isa.ideas.repo.impl.fs.FSWorkspace;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveWorkspace extends Workspace {

	private static final Logger LOGGER = Logger.getLogger(GDriveWorkspace.class
			.getName());
	public GDriveWorkspace(String name, String owner) {
		super(name, owner);

	}

	@Override
	protected boolean moveImpl(Listable dest, boolean copy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean deleteImpl() {
		boolean res=false;
		//Obtenemos el workspace por el nombre
		try {
		File workspace=DriveQuickstart.getWorkspaceByName(this.getName(),this.getOwner());
		if(workspace==null) {
			
			LOGGER.log(Level.INFO, "Workspace "+this.getName()+" does not exist.");
		}else {
			DriveQuickstart.driveService().files().delete(workspace.getId()).execute();
			res=true;
		}
		
		}catch(IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	

	@Override
	protected boolean persistImpl() {
		boolean res=false;
		//Primero revisa si esta la carpeta del repositorio
		try {
		checkOwnerFolder();
		File fileMetadata = new File();
		fileMetadata.setName(this.getName());
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		//Cada workspace se guarda dentro de la carpeta repositorio
		fileMetadata.setParents(Collections.singletonList(DriveQuickstart.getRepoFolder(this.getOwner()).getId()));
		//Antes de guardar el workspace comprobamos que no existe otro con el mismop nombre
		if(DriveQuickstart.getFilesByFolderId(DriveQuickstart.getRepoFolder(this.getOwner()).getId()).contains(fileMetadata.getName())) {
			LOGGER.log(Level.INFO,"Workspace already exist");
		}else {
		DriveQuickstart.driveService().files().create(fileMetadata)
	    .setFields("id")
	    .execute();
		}
		}catch(IOException |GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}


	private void checkOwnerFolder() throws GeneralSecurityException, IOException {
		// Si no hay ninguna carpeta crea la carpeta del repositorio
		if (DriveQuickstart.getRepoFolder(this.getOwner())==null) {
			File fileMetadata = new File();
			fileMetadata.setName(this.getOwner());
			fileMetadata.setMimeType("application/vnd.google-apps.folder");
			DriveQuickstart.driveService().files().create(fileMetadata).setFields("id").execute();
		}

	}

	@Override
	protected FSNode listImpl(){
		//Buscar un workspace por el nombre (this.getName())
		//Crea un nodo de ese workspace
		FSNode res=new FSNode();
		res.setTitle(this.getName());
		res.setFolder(true);
		res.setIcon(FSNodeIcon.WORKSPACE);
		List<Node> children=new ArrayList<>();
		try {
		File workspace=DriveQuickstart.getWorkspaceByName(this.getName(),this.getOwner());
		if(workspace==null) {
			LOGGER.log(Level.INFO, "Workspace "+this.getName()+" does not exist.");
			res=null;
		}
		List<String> files=new ArrayList<String>(DriveQuickstart.getFilesByFolderId(workspace.getId()));
		//Los hijos del nodo son los ficheros de ese workspace
		for(String f:files) {
			FSNode child=new FSNode();
			child.setTitle(f);
			children.add(child);
			
		}
		res.setChildren(children);

		}catch(IOException| GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}
	

	public boolean exist()   {
		boolean res=false;
		try {
		if(DriveQuickstart.getWorkspaceByName(this.getName(),this.getOwner())!=null) {
			res=true;
		}
		}
		catch(IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}

}
