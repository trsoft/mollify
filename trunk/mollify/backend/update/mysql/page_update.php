<?php

	/**
	 * Copyright (c) 2008- Samuli J�rvel�
	 *
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the Eclipse Public License v1.0
	 * which accompanies this distribution, and is available at
	 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
	 * this entire header must remain intact.
	 */
	 
	 include("install/installation_page.php");	 
?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
	<?php pageHeader("Mollify Update", "init"); ?>
	
	<body id="page-mysql-current-installed">
		<?php pageBody("Update", "Database Update"); ?>
		<div class="content">
			<p>
				Installed Mollify version is <b><?php echo $installer->versionString($installer->installedVersion()) ?></b>, and the current version is <b><?php echo $installer->versionString($installer->currentVersion()) ?></b>.
			</p>
			<p>
				To update Mollify to current version, click "Update".
			</p>
			<p>
				<a id="button-update" href="#" class="btn green">Update</a>
			</p>
		</div>
		<?php pageFooter(); ?>
	</body>
	
	<script type="text/javascript">
		function init() {
			$("#button-update").click(function() {
				action("update");
			});
		}
	</script>
</html>