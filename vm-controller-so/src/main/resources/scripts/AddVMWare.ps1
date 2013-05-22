$serverName = $args[0]
$userName = $args[1]
$password = $args[2]
$vmName = $args[3]
$hostName = $args[4]
$tempName = $args[5]
$specName = $args[6]
$dataName = $args[7]

set-alias Get-VIServer Connect-VIServer

$vc = Get-VIServer -Server $serverName -User $userName -Password $password

$host1 = Get-VMHost -Server $vc -Name $hostName

$spec = Get-OSCustomizationSpec $specName

$temp = Get-Template -Server $vc | where {$_.name -like $tempName}

$data = Get-Datastore $dataName

$vm = New-VM -Name $vmName -Template $temp -Host $host1 -OSCustomizationSpec $spec -Datastore $data
$vm