<?php
	require_once("Features.class.php");
	
	class ServiceEnvironment {
		private $services = array();
		private $session;
		private $authentication; 
		private $responseHandler;
		private $configurationProvider;
		private $settings;
		private $filesystem;
		
		public function __construct($session, $responseHandler, $configurationProvider, $settings) {
			$this->session = $session;
			$this->responseHandler = $responseHandler;
			$this->configurationProvider = $configurationProvider;
			$this->settings = $settings;
			$this->features = new Features($configurationProvider, $settings);
			$this->authentication = new Authentication($this); 
			$this->filesystem = new Filesystem($this);
		}
		
		public function session() {
			return $this->session;
		}

		public function response() {
			return $this->responseHandler;
		}
		
		public function authentication() {
			return $this->authentication;
		}
		
		public function configuration() {
			return $this->configurationProvider;
		}

		public function features() {
			return $this->features;
		}

		public function filesystem() {
			return $this->filesystem;
		}

		public function settings() {
			return $this->settings;
		}
						
		public function initialize($request) {
			$this->session->initialize($request);
			$this->filesystem->initialize($request);
			$this->authentication->initialize($request);
			$this->log();
		}
		
		public function onSessionStarted() {
			$this->filesystem->onSessionStarted();
		}
						
		public function addService($path, $controller) {
			$this->services[$path] = $controller;
		}
		
		public function getService($request) {
			$path = $request->path();
			$id = $path[0];
			if (!array_key_exists($id, $this->services)) throw new ServiceException("Unknown service '".$id."'");
			$service = $this->createService($this->services[$id], $request, array_slice($path, 1));
			if (Logging::isDebug()) $service->log();
			return $service;
		}
		
		private function createService($controller, $request, $path) {
			require_once("services/ServicesBase.class.php");
			require_once("services/".$controller.".class.php");
			return new $controller($this, $request, $path);
		}
		
		public function log() {
			if (!Logging::isDebug()) return;
			Logging::logSystem();
			
			$this->settings->log();
			$this->configurationProvider->log();
			$this->features->log();
			$this->filesystem->log();
			$this->session->log();
			$this->authentication->log();
		}
	}

	class ServiceException extends Exception {
		private $type;
		
		public function __construct($type, $details = "") {
			parent::__construct($details);
			$this->type = $type;
		}
		
		function type() {
			return $this->type;
		}
		
		function details() {
			return $this->getMessage();
		}
	}
?>