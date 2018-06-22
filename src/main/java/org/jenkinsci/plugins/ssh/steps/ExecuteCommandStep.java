package org.jenkinsci.plugins.ssh.steps;

import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import java.io.IOException;
import lombok.Getter;
import org.jenkinsci.plugins.ssh.util.SSHMasterToSlaveCallable;
import org.jenkinsci.plugins.ssh.util.SSHStepDescriptorImpl;
import org.jenkinsci.plugins.ssh.util.SSHStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Step to execute a command on remote node.
 *
 * @author Naresh Rayapati
 */
public class ExecuteCommandStep extends BasicSSHStep {

  private static final long serialVersionUID = 7492916747486604582L;

  @Getter
  private final String command;

  @Getter
  @DataBoundSetter
  private boolean sudo = false;

  @DataBoundConstructor
  public ExecuteCommandStep(final String command) {
    this.command = command;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new Execution(this, context);
  }

  @Extension
  public static class DescriptorImpl extends SSHStepDescriptorImpl {

    @Override
    public String getFunctionName() {
      return "sshExecuteCommand";
    }

    @Override
    public String getDisplayName() {
      return getPrefix() + "Execute command on remote node.";
    }
  }

  public static class Execution extends SSHStepExecution {

    private static final long serialVersionUID = -5293952534324828128L;

    protected Execution(final ExecuteCommandStep step, final StepContext context)
        throws IOException, InterruptedException {
      super(step, context);
    }

    @Override
    protected Object run() throws Exception {
      ExecuteCommandStep step = (ExecuteCommandStep) getStep();
      if (Util.fixEmpty(step.getCommand()) == null) {
        throw new IllegalArgumentException("command is null or empty");
      }
      return getLauncher().getChannel().call(new ExecuteCommandCallable(step, getListener()));
    }

    private static class ExecuteCommandCallable extends SSHMasterToSlaveCallable {

      public ExecuteCommandCallable(final ExecuteCommandStep step, final TaskListener listener) {
        super(step, listener);
      }

      @Override
      public Object execute() {
        ExecuteCommandStep step = (ExecuteCommandStep) getStep();
        return getService().executeCommand(step.getCommand(), step.isSudo());
      }
    }
  }
}