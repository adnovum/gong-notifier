package ch.adnovum.gong.notifier.buildstatus;

public class MaterialInfo {
	private MaterialType materialType;
	private String url;
	private RepoCoordinates repoCoordinates;
	private String revision;

	public static class RepoCoordinates {
		private final String project;
		private final String repo;

		public RepoCoordinates(String project, String repo) {
			this.project = project;
			this.repo = repo;
		}

		public String getProject() {
			return project;
		}

		public String getRepo() {
			return repo;
		}

		@Override
		public String toString() {
			return "RepoCoordinates{" +
					"project='" + project + '\'' +
					", repo='" + repo + '\'' +
					'}';
		}
	}

	public enum MaterialType {
		GIT,
		SCM
	}

	MaterialType getMaterialType() {
		return materialType;
	}

	void setMaterialType(MaterialType materialType) {
		this.materialType = materialType;
	}

	String getUrl() {
		return url;
	}

	void setUrl(String url) {
		this.url = url;
	}

	public RepoCoordinates getRepoCoordinates() {
		return repoCoordinates;
	}

	void setRepoCoordinates(RepoCoordinates repoCoordinates) {
		this.repoCoordinates = repoCoordinates;
	}

	public String getRevision() {
		return revision;
	}

	void setRevision(String revision) {
		this.revision = revision;
	}
}
