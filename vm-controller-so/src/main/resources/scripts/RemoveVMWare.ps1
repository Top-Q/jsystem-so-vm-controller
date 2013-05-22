$serverName = $args[0]
$userName = $args[1]
$password = $args[2]
$vmName = $args[3]

set-alias Get-VIServer Connect-VIServer

$vc = Get-VIServer -Server $serverName -User $userName -Password $password
$vc

$vm = Remove-VM -VM $vmName -Confirm:$false -DeleteFromDisk
$vm