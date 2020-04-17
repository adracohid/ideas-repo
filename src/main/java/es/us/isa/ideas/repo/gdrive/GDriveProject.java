package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.IdeasRepo;
import es.us.isa.ideas.repo.Project;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSNodeIcon;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveProject extends Project {
	private static final Logger LOGGER = Logger.getLogger(GDriveProject.class.getName());
	private Drive credentials;
	public GDriveProject(String name, String workspace, String owner, Drive credentials) {
		super(name, workspace, owner);
		this.credentials=credentials;
	}

	public Drive getCredentials() {
		return credentials;
	}

	public void setCredentials(Drive credentials) {
		this.credentials = credentials;
	}

	@Override
	protected boolean moveImpl(Listable dest, boolean copy) {
		boolean res = false;
		try {
			File project = DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner(),this.credentials);
					
			if (project == null) {
				LOGGER.log(Level.INFO, "Project" + this.getName() + " does not exist.");

			} else {
				//Comprobamos que la carpeta destino sea un workspace
				if(!(dest instanceof GDriveWorkspace)) {
					LOGGER.log(Level.INFO, "The target folder must be a workspace");
				}else {
				System.out.println(IdeasRepo.get().getObjectFullUri(dest));
				String[] s1 = IdeasRepo.get().getObjectFullUri(dest).split("//");
				String[] s2 = s1[1].split("/");
				File workspace = DriveQuickstart.getWorkspaceByName(s2[s2.length-1], s2[s2.length-2],this.credentials);
				
				if (workspace== null) {
					LOGGER.log(Level.INFO, "Workspace " + dest.getClass().getName() + " does not exist.");

				} else {
				//Comprobamos si el proyecto existe dentro del workspace
				File projectInWorkspace=DriveQuickstart.getProjectByName(this.getName(), workspace.getName(), this.getOwner(), credentials);
				
					if (projectInWorkspace==null) {
						// En este caso solo se permite mover carpetas
						DriveQuickstart.moveFileToFolder(project.getId(), workspace.getId(),credentials);
						res=true;
					} else {
						LOGGER.log(Level.INFO, "There is a folder with the same name in the target workspace. ");

					}
				}
			}
			}
		} catch (IOException | GeneralSecurityException|ObjectClassNotValidException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	protected boolean deleteImpl() {
		boolean res=false;
		try {

		//Obtenemos el proyecto
		File project=DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner(),this.credentials);	
		//Comprobamos que existe
		if(  project==null) {
			LOGGER.log(Level.INFO,  "Project "+this.getName()+" does not exist.");
		}else {
			this.credentials.files().delete(project.getId()).execute();
			res=true;
		}
		
		}catch(IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	protected boolean persistImpl() {
		boolean res = false;
		
		try {
			File project=DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner(), this.credentials);
			// Obtener la id del workspace donde se vaya a guardar el proyecto
			File workspace = DriveQuickstart.getWorkspaceByName(this.getWorkspace(), this.getOwner(),this.credentials);
			// Comprueba si existe tanto el workspace como el proyecto
			if(workspace==null) {
				LOGGER.log(Level.INFO, "Workspace "+this.getWorkspace()+" does not exist.");
			}else if(project!=null){
				LOGGER.log(Level.INFO, "Proyect " + this.getName() + " already exist");
			}else {
			File fileMetadata = new File();
			fileMetadata.setName(this.getName());
			fileMetadata.setMimeType("application/vnd.google-apps.folder");
			// Meter el proyecto dentro de la carpeta
			fileMetadata.setParents(Collections.singletonList(workspace.getId()));
			//Comprobamos que el workspace existe
			
			
			// Comprobamos que no se crea un proyecto con el mismo nombre						
			
				// Guardar el proyecto
				this.credentials.files().create(fileMetadata).setFields("id").execute();
				res=true;

			
			}
		} catch (IOException | GeneralSecurityException e) {

			e.printStackTrace();
		}
		// Guardar el proyecto dentro de esa carpeta
		return res;
	}

	//TODO
	@Override
	protected FSNode listImpl() {
		//Buscar un proyecto por su nombre
		//Crear un nodo de ese proyecto
		FSNode res=new FSNode();
		res.setTitle(this.getName());
		res.setFolder(true);
		res.setIcon(FSNodeIcon.PROJECT);
		String path=this.getWorkspace()+"/"+this.getName();
		res.setKeyPath(path);
		try {
			File project=DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner(),this.credentials);
			if(project==null) {
				LOGGER.log(Level.INFO,  "Project "+this.getName()+" does not exist.");
				res=null;
			}else {
			res.setChildren(GDriveNode.getChildren(res.getKeyPath(),res,project.getId(),credentials));
			
			}
			
		}catch(IOException| GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	

	public boolean exist()  {
		boolean res=false;
		try {
		if(DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner(), this.credentials)!=null) {
			res=true;
		}
		}catch(IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}

}
