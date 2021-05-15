package com.gdmrdigital.iiif.model.github;

public class GitHubObj {
    protected String _sha = "";
    public GitHubObj() {
    }

    public void setSha(final String pSha) {
        _sha = pSha;
    }

    public String getSha(){
        return _sha;
    }
}
