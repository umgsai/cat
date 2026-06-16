package com.dianping.cat.home.router.transform;

import java.util.ArrayList;
import java.util.List;
import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.Group;
import com.dianping.cat.home.router.entity.GroupServer;
import com.dianping.cat.home.router.entity.Network;
import com.dianping.cat.home.router.entity.NetworkPolicy;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.entity.ServerGroup;

public class DefaultLinker implements ILinker {
   private boolean m_deferrable;

   private List<Runnable> m_deferedJobs = new ArrayList<Runnable>();

   public DefaultLinker(boolean deferrable) {
      m_deferrable = deferrable;
   }

   public void finish() {
      for (Runnable job : m_deferedJobs) {
         job.run();
      }
   }

   @Override
   public boolean onDefaultServer(final RouterConfig parent, final DefaultServer defaultServer) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addDefaultServer(defaultServer);
            }
         });
      } else {
         parent.addDefaultServer(defaultServer);
      }

      return true;
   }

   @Override
   public boolean onDomain(final RouterConfig parent, final Domain domain) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addDomain(domain);
            }
         });
      } else {
         parent.addDomain(domain);
      }

      return true;
   }

   @Override
   public boolean onGroup(final Domain parent, final Group group) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addGroup(group);
            }
         });
      } else {
         parent.addGroup(group);
      }

      return true;
   }

   @Override
   public boolean onGroupServer(final ServerGroup parent, final GroupServer groupServer) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addGroupServer(groupServer);
            }
         });
      } else {
         parent.addGroupServer(groupServer);
      }

      return true;
   }

   @Override
   public boolean onNetwork(final NetworkPolicy parent, final Network network) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addNetwork(network);
            }
         });
      } else {
         parent.addNetwork(network);
      }

      return true;
   }

   @Override
   public boolean onNetworkPolicy(final RouterConfig parent, final NetworkPolicy networkPolicy) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addNetworkPolicy(networkPolicy);
            }
         });
      } else {
         parent.addNetworkPolicy(networkPolicy);
      }

      return true;
   }

   @Override
   public boolean onServer(final Group parent, final Server server) {
      parent.addServer(server);
      return true;
   }

   @Override
   public boolean onServerGroup(final RouterConfig parent, final ServerGroup serverGroup) {
      if (m_deferrable) {
         m_deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addServerGroup(serverGroup);
            }
         });
      } else {
         parent.addServerGroup(serverGroup);
      }

      return true;
   }
}
