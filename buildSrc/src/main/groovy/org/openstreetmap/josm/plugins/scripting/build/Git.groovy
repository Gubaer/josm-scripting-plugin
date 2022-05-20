package org.openstreetmap.josm.plugins.scripting.build

class Git {

    static private def executeAndLog(cmd) {
        def proc = cmd.execute()
        proc.waitFor()
        println "Exit Value: ${proc.exitValue()}"
        println "Output: ${proc.in.text}"
        println "Error: ${proc.err.text}"
        if (proc.exitValue() != 0) {
            throw new Error("git command failed. command was <$cmd>")
        }
    }

    static def ensureOnBranch(branch) {
        def proc = "git rev-parse --abbrev-ref HEAD".execute()
        def ret = proc.in.text.trim()
        if (ret != branch) {
            throw new Error("currently on branch '$ret', but expected branch '$branch'")
        }
    }

    static def add(file) {
        executeAndLog(["git", "add", file])
    }

    static def commit(file, message) {
        executeAndLog(["git", "commit", "-m", message, file])
    }

    static def push(remote, branch) {
        executeAndLog("git push $remote $branch")
    }

    static def tagHead(tag) {
        executeAndLog(["git", "tag", "-f", tag, "HEAD"])
    }

    static def pushTags() {
        executeAndLog("git push --tags -f")
    }
}
