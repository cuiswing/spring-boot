/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ReflectionUtils;

/**
 * SpringApplicationRunListeners类的主要作用就是存储监听器对象集合并发布各种监听事件,
 * SpringApplicationRunListeners其本质上就是一个事件对象存储和发布者,
 * 它在SpringBoot应用启动的不同时间点委托给ApplicationEventMulticaster发布不同应用事件类型(ApplicationEvent)
 * <p>
 * SpringApplicationRunListeners和SpringApplicationRunListener的关系：
 * <p>
 * SpringApplicationRunListeners中包含了private final List<SpringApplicationRunListener> listeners集合
 * 真正负责事件发布的是SpringApplicationRunListener
 * <p>
 * <p>
 * SpringApplicationRunListener中又维护了SimpleApplicationEventMulticaster对象,并通过该对象将事件广播给各个监听器
 *
 * <p>
 * A collection of {@link SpringApplicationRunListener}.
 *
 * @author Phillip Webb
 */
class SpringApplicationRunListeners {

	private final Log log;

	private final List<SpringApplicationRunListener> listeners;

	// 接受一个Log和Collection对象并赋给类成员变量
	SpringApplicationRunListeners(Log log,
			Collection<? extends SpringApplicationRunListener> listeners) {
		this.log = log;
		this.listeners = new ArrayList<>(listeners);
	}

	//首次启动run方法时立即调用。
	public void starting() {
		// this.listeners集合中包含了EventPublishingRunListener实例,那么这里将要调用其starting()方法
		for (SpringApplicationRunListener listener : this.listeners) {
			listener.starting();
		}
	}

	// 一旦准备好环境，但在ApplicationContext创建环境之前调用 。
	public void environmentPrepared(ConfigurableEnvironment environment) {
		for (SpringApplicationRunListener listener : this.listeners) {
			listener.environmentPrepared(environment);
		}
	}

	// ApplicationContext在创建和准备之后调用，但在加载源之前调用。
	public void contextPrepared(ConfigurableApplicationContext context) {
		for (SpringApplicationRunListener listener : this.listeners) {
			listener.contextPrepared(context);
		}
	}

	// 在应用程序上下文加载之后但在刷新之前调用。
	public void contextLoaded(ConfigurableApplicationContext context) {
		for (SpringApplicationRunListener listener : this.listeners) {
			listener.contextLoaded(context);
		}
	}

	// 上下文已被刷新，并且应用程序已启动，且CommandLineRunners和ApplicationRunners未被调用。
	public void started(ConfigurableApplicationContext context) {
		for (SpringApplicationRunListener listener : this.listeners) {
			listener.started(context);
		}
	}

	// 在run方法完成之前立即调用，应用上下文已经被刷新,并且CommandLineRunners和ApplicationRunners已经被调用
	public void running(ConfigurableApplicationContext context) {
		for (SpringApplicationRunListener listener : this.listeners) {
			listener.running(context);
		}
	}

	// 在运行应用程序时发生故障时调用。
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		for (SpringApplicationRunListener listener : this.listeners) {
			callFailedListener(listener, context, exception);
		}
	}

	private void callFailedListener(SpringApplicationRunListener listener,
			ConfigurableApplicationContext context, Throwable exception) {
		try {
			listener.failed(context, exception);
		}
		catch (Throwable ex) {
			if (exception == null) {
				ReflectionUtils.rethrowRuntimeException(ex);
			}
			if (this.log.isDebugEnabled()) {
				this.log.error("Error handling failed", ex);
			}
			else {
				String message = ex.getMessage();
				message = (message != null) ? message : "no error message";
				this.log.warn("Error handling failed (" + message + ")");
			}
		}
	}

}
