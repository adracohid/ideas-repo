package es.us.isa.ideas.repo.gdrive;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.drive.model.File;

import es.us.isa.ideas.repo.IdeasRepo;
import es.us.isa.ideas.repo.Node;
import es.us.isa.ideas.repo.Project;
import es.us.isa.ideas.repo.exception.ObjectClassNotValidException;
import es.us.isa.ideas.repo.impl.fs.FSNode;
import es.us.isa.ideas.repo.impl.fs.FSNodeIcon;
import es.us.isa.ideas.repo.operation.Listable;

public class GDriveProject extends Project {
	private static final Logger LOGGER = Logger.getLogger(GDriveProject.class.getName());

	public GDriveProject(String name, String workspace, String owner) {
		super(name, workspace, owner);
	}

	@Override
	protected boolean moveImpl(Listable dest, boolean copy) {
		boolean res = false;
		try {
			File project = DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner());
					
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
				File workspace = DriveQuickstart.getWorkspaceByName(s2[s2.length-1], s2[s2.length-2]);
				
				if (workspace== null) {
					LOGGER.log(Level.INFO, "Workspace " + dest.getClass().getName() + " does not exist.");

				} else {
					if (!DriveQuickstart.getFilesByFolderId(workspace.getId()).contains(project.getName())) {
						// En este caso solo se permite mover carpetas
						DriveQuickstart.moveFileToFolder(project.getId(), workspace.getId());
						res=true;
					} else {
						LOGGER.log(Level.INFO, "Cannot move a folder with the same name.");

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
		File project=DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner());	
		//Comprobamos que existe
		if(  project==null) {
			LOGGER.log(Level.INFO,  "Project "+this.getName()+" does not exist.");
		}else {
			DriveQuickstart.driveService().files().delete(project.getId()).execute();
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
			// Obtener la id del workspace donde se vaya a guardar el proyecto
			File workspace = DriveQuickstart.getWorkspaceByName(this.getWorkspace(), this.getOwner());
			// Crear el proyecto
			if(workspace==null) {
				LOGGER.log(Level.INFO, "Workspace "+this.getWorkspace()+" does not exist.");
			}else {
			File fileMetadata = new File();
			fileMetadata.setName(this.getName());
			fileMetadata.setMimeType("application/vnd.google-apps.folder");
			// Meter el proyecto dentro de la carpeta
			fileMetadata.setParents(Collections.singletonList(workspace.getId()));
			//Comprobamos que el workspace existe
			
			
			// Comprobamos que no se crea un proyecto con el mismo nombre						
			if (DriveQuickstart.getFilesByFolderId(workspace.getId()).contains(fileMetadata.getName())) {
				LOGGER.log(Level.INFO, "Proyect " + this.getName() + " already exist");

			} else {
				// Guardar el proyecto
				DriveQuickstart.driveService().files().create(fileMetadata).setFields("id").execute();
				res=true;

			}
			}
		} catch (IOException | GeneralSecurityException e) {

			e.printStackTrace();
		}
		// Guardar el proyecto dentro de esa carpeta
		return res;
	}

	@Override
	protected FSNode listImpl() {
		//Buscar un proyecto por su nombre
		//Crear un nodo de ese proyecto
		FSNode res=new FSNode();
		res.setTitle(this.getName());
		res.setFolder(true);
		res.setIcon(FSNodeIcon.PROJECT);
		List<Node> children=new ArrayList<>();
		try {
			File project=DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner());
			if(project==null) {
				LOGGER.log(Level.INFO,  "Project "+this.getName()+" does not exist.");
				res=null;
			}else {
			List<String> files=new ArrayList<>(DriveQuickstart.getFilesByFolderId(project.getId()));
			//Los hijos del nodo son todos los ficheros de ese proyecto
			for(String f:files) {
				FSNode child=new FSNode();
				child.setTitle(f);
				children.add(child);
			}
		res.setChildren(children);
			}
			
		}catch(IOException| GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public boolean exist()  {
		boolean res=false;
		try {
		if(DriveQuickstart.getProjectByName(this.getName(), this.getWorkspace(), this.getOwner())!=null) {
			res=true;
		}
		}catch(IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		return res;
	}

}
