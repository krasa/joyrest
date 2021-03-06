package org.joyrest.routing;

import net.jodah.typetools.TypeResolver;
import org.joyrest.function.TriConsumer;
import org.joyrest.model.RoutePart;
import org.joyrest.model.http.HttpMethod;
import org.joyrest.model.request.Request;
import org.joyrest.model.response.Response;
import org.joyrest.processor.RequestProcessor;
import org.joyrest.utils.PathUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * Class {@link AbstractControllerConfiguration} is abstract implementation of
 * {@link ControllerConfiguration} and makes easier to create the given route
 * using predefined protected method.
 * <p>
 * It can be considered as container for routes which are provided to
 * {@link RequestProcessor} because of processing
 * and handling incoming requests.
 *
 * @author pbouda
 */
public abstract class AbstractControllerConfiguration implements ControllerConfiguration {

	/* Set of routes which are configured in an inherited class  */
	private final Set<AbstractRoute<?, ?>> routes = new HashSet<>();

	/* Class validates and customized given path */
	private final PathCorrector pathCorrector = new PathCorrector();

	/* Resource path that will be added to the beginning of all routes defined in the inherited class */
	private String globalPath = null;

	/* RoutingConfiguration's initialization should be executed only once */
	private boolean isInitialized = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void initialize() {
		if (!this.isInitialized) {
			configure();

			List<RoutePart<String>> globalParts = PathUtils.createRouteParts(globalPath);

			if (globalPath != null) {
				this.routes.stream().forEach(route -> route.addGlobalPath(globalParts));
			}

			this.isInitialized = true;
		}
	}

	/**
	 * Convenient place where is possible to configure new routes for this instance of {@link ControllerConfiguration}
	 */
	abstract protected void configure();

	/**
	 * Creates a resource part of the path unified for all routes defined in the inherited class
	 *
	 * @param path resource path of all defined class
	 * @throws NullPointerException whether {@code path} is {@code null}
	 */
	protected final void setGlobalPath(String path) {
		requireNonNull(path, "Global path cannot be change to 'null'");

		if (!"".equals(path) || !"/".equals(path)) {
			this.globalPath = pathCorrector.apply(path);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<AbstractRoute<?, ?>> getRoutes() {
		return routes;
	}

	protected <REQ, RESP> EntityRoute<REQ, RESP> createEntityRouteFromTri(HttpMethod method, String path,
											TriConsumer<Request<REQ>, Response<RESP>, REQ> action, Class<REQ> clazz) {
		final String correctPath = pathCorrector.apply(path);
		final EntityRoute<REQ, RESP> route = new EntityRoute<>(correctPath, method, action, clazz);
		routes.add(route);
		return route;
	}

	protected <REQ, RESP> EntityRoute<REQ, RESP> createEntityRouteFromBi(HttpMethod method, String path,
				BiConsumer<Request<REQ>, Response<RESP>> action, Class<REQ> reqClazz, Class<RESP> respClazz) {
		final String correctPath = pathCorrector.apply(path);
		final EntityRoute<REQ, RESP> route = new EntityRoute<>(correctPath, method, action, reqClazz, respClazz);
		routes.add(route);
		return route;
	}

	@SuppressWarnings("unchecked")
	protected <REQ, RESP> Class<REQ> getActionBodyClass(TriConsumer<Request<REQ>, Response<RESP>, REQ> action) {
		return (Class<REQ>) TypeResolver.resolveRawArguments(TriConsumer.class, action.getClass())[2];
	}
}