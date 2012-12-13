package org.ngrinder.agent.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.grinder.message.console.AgentControllerState;

import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;

public class AgentManagerServiceTestForDist extends AbstractNGrinderTransactionalTest {

	@Autowired
	AgentManager agentManager;

	@Autowired
	AgentManagerRepository agentRepository;

	Config config;

	AgentManagerService agentManagerService;

	@Before
	public void before() {
	}

	public AgentInfo createAgentInfo(String region, boolean approved, AgentControllerState status) {
		AgentInfo agentInfo1 = new AgentInfo();
		agentInfo1.setRegion(region);
		agentInfo1.setApproved(approved);
		agentInfo1.setStatus(status);
		return agentInfo1;
	}

	@Before
	public void init() {

		agentManagerService = new ClusteredAgentManagerService() {
			@SuppressWarnings("serial")
			@Override
			public List<AgentInfo> getAllActiveAgentInfoFromDB() {
				return new ArrayList<AgentInfo>() {
					{
						add(createAgentInfo("hello", true, AgentControllerState.READY));
						add(createAgentInfo("hello", true, AgentControllerState.READY));
						add(createAgentInfo("hello_owned_wow", true, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha", false, AgentControllerState.READY));
						add(createAgentInfo("haha", true, AgentControllerState.READY));
						add(createAgentInfo("haha_owned_my", true, AgentControllerState.READY));
						add(createAgentInfo("woowo_owned_my", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", true, AgentControllerState.READY));
						add(createAgentInfo("wowo", false, AgentControllerState.READY));
						add(createAgentInfo("kiki", false, AgentControllerState.READY));

					}
				};
			}

			@SuppressWarnings("serial")
			@Override
			List<String> getRegions() {
				List<String> regions = new ArrayList<String>() {
					{
						add("hello");
						add("haha");
						add("wowo");
					}
				};
				return regions;
			}

			@Override
			int getMaxAgentSizePerConsole() {
				return 3;
			}
		};

		config = mock(Config.class);
		when(config.isCluster()).thenReturn(true);
		agentManagerService.setConfig(config);
		agentManagerService.setAgentManager(this.agentManager);
		agentManagerService.setAgentRepository(this.agentRepository);
	}

	@Test
	public void test() {

		User user = new User();
		user.setUserId("haha");
		Map<String, MutableInt> userAvailableAgentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		System.out.println(userAvailableAgentCountMap);
		assertThat(userAvailableAgentCountMap.containsKey("kiki"), is(false));
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));

		user.setUserId("wow");
		userAvailableAgentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(3));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(3));

		user.setUserId("my");
		userAvailableAgentCountMap = agentManagerService.getUserAvailableAgentCountMap(user);
		assertThat(userAvailableAgentCountMap.get("hello").intValue(), is(2));
		assertThat(userAvailableAgentCountMap.get("haha").intValue(), is(4));
		assertThat(userAvailableAgentCountMap.get("wowo").intValue(), is(3));

	}
}