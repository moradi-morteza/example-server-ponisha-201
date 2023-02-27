<?php

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $data_array = file_get_contents('php://input');
    $people = json_decode($data_array, true);

    // Save the list of people to a JSON file
    $filename = 'items.json';
    $fp = fopen($filename, 'w');
    fwrite($fp, json_encode($people));
    fclose($fp);

    $response = array(
        'code' => 200,
        'message' => 'Data saved successfully'
    );
    header('Content-Type: application/json');
    echo json_encode($response);

} else {
    $response = array(
            'code' => 405,
            'message' => 'Method not allowed'
    );

    header('Content-Type: application/json');
    echo json_encode($response);
}