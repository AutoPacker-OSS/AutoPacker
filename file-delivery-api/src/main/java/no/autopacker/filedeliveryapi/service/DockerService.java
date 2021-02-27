package no.autopacker.filedeliveryapi.service;

import no.autopacker.filedeliveryapi.config.DockerConfig;
import no.autopacker.filedeliveryapi.database.DockerfileRepository;
import no.autopacker.filedeliveryapi.domain.ModuleMeta;
import no.autopacker.filedeliveryapi.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class DockerService {

	private DockerfileRepository dockerfileRepo;
	private DockerConfig dockerConfig;
	private Logger logger;

	@Autowired
	public DockerService(DockerfileRepository dockerfileRepo,
	                            DockerConfig dockerConfig) throws Exception {
		this.dockerfileRepo = dockerfileRepo;
		this.dockerConfig = dockerConfig;
		this.logger = LoggerFactory.getLogger(this.getClass());

		initialize();
	}


	/**
	 * Initialize the DockerService instance.
	 */
	private void initialize() {
		if (!this.isDockerInstalled()) {
			throw new BeanCreationException("Docker is not found! Please have Docker installed and make sure its " +
					"root folder is added to the user/system PATH variable.");
		}

		// Get Docker status command of the login attempt
		int statusCode = this.loginToDocker();

		if (statusCode != 0) {
			throw new BeanCreationException("Couldn't login to Docker");
		}

		logger.info("Logged into Docker as " + dockerConfig.getUsername() + "!");
	}

	/**
	 * Runs the docker command to check if Docker is installed and available globally.
	 * @return  {@code true} if Docker is installed, otherwise it returns {@code false}
	 */
	private boolean isDockerInstalled() {
		boolean isInstalled;

		try {
			Runtime.getRuntime().exec(new String[]{"docker", "--help"});
			isInstalled = true;
		} catch (IOException e) {
			isInstalled = false;
		}

		return isInstalled;
	}

	/**
	 * Login to docker and store the system status code.
	 *
	 * @return              0 (OK) if Docker is logged in or another number if login failed
	 */
	public int loginToDocker() {
		int statusCode;

		try {
			// Login to docker
			statusCode = Runtime
					.getRuntime()
					.exec(
							new String[]{"docker", "login", "--username", dockerConfig.getUsername(),
									"--password", dockerConfig.getToken(), dockerConfig.getRepository()})
					.waitFor();
		} catch (IOException | InterruptedException e) {
			statusCode = -1;
		}

		return statusCode;
	}

	public void buildDockerImage(ModuleMeta module, String ownerUsername) throws Exception {
		String location = module.getLocation();

		String dockerFileLocation = dockerfileRepo.findByName(module.getConfigType()).getLocation();

		String moduleImageName = Utils.instance().getModuleImageName(ownerUsername, module.getName());
		Runtime cmd = Runtime.getRuntime();

		File dockerFileSource = new File(dockerFileLocation);
		File dockerFileDestination = new File(location.concat("Dockerfile"));

		// Copy dockerfile from repository and save it inside project dir with the standard name "Dockerfile"
		FileUtils.copyFile(dockerFileSource, dockerFileDestination);

		// Build docker image (the repo part format: autopacker/username-module)
		cmd.exec(new String[]{"docker", "build", "-t", dockerConfig.getUsername().concat("/").concat(moduleImageName),
				location}).waitFor();

		// Push docker image to repo
		cmd.exec(new String[]{"docker", "push", dockerConfig.getUsername() + "/" + moduleImageName}).waitFor();
	}
}
