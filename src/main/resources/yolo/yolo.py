import os
import sys
import time

import cv2
import numpy as np


class Detection:
    def __init__(self, class_confidences, center_coordinates, dimensions, image_width, image_height):
        self.class_confidences = class_confidences
        self.center_coordinates = center_coordinates
        self.dimensions = dimensions
        self._image_width = image_width
        self._image_height = image_height

    @property
    def most_confident_class_id(self):
        return np.argmax(self.class_confidences)

    @property
    def most_confident_class_id_confidence(self):
        return float(self.class_confidences[self.most_confident_class_id])

    @property
    def bounding_box(self):
        return BoundingBox(center_x=self.center_coordinates[0],
                           center_y=self.center_coordinates[1],
                           width=self.dimensions[0],
                           height=self.dimensions[1],
                           width_scale=self._image_width,
                           height_scale=self._image_height)


class BoundingBox:
    def __init__(self, width, height, center_x, center_y, width_scale, height_scale):
        self._width = width
        self._height = height
        self._center_x = center_x
        self._center_y = center_y
        self._width_scale = width_scale
        self._height_scale = height_scale

    @property
    def width(self):
        return int(self._width * self._width_scale)

    @property
    def height(self):
        return int(self._height * self._height_scale)

    @property
    def center_x(self):
        return int(self._center_x * self._width_scale)

    @property
    def center_y(self):
        return int(self._center_y * self._height_scale)

    @property
    def x_start(self):
        return int(self.center_x - self.width / 2)

    @property
    def x_end(self):
        return int(self.center_x + self.width / 2)

    @property
    def y_start(self):
        return int(self.center_y - self.height / 2)

    @property
    def y_end(self):
        return int(self.center_y + self.height / 2)


class ImageObjectDetector:
    def __init__(self, neural_network, labels, confidence_threshold, non_maxima_suppression_threshold, image,
                 save_directory):
        self._neural_network = neural_network
        self._labels = labels
        self.confidence_threshold = confidence_threshold
        self._non_maxima_suppression_threshold = non_maxima_suppression_threshold
        self._image = image
        self.detected_object_bounding_boxes = []
        self.detected_object_confidences = []
        self.save_directory = save_directory

    @property
    def image_width(self):
        return self._image.shape[1]

    @property
    def image_height(self):
        return self._image.shape[0]

    def perform_object_detection_on_image(self, image):
        layer_names = self._neural_network.getLayerNames()
        unconnected_layer_names = [layer_names[i[0] - 1] for i in self._neural_network.getUnconnectedOutLayers()]

        blob = cv2.dnn.blobFromImage(image, 1 / 255.0, (416, 416),
                                     swapRB=True, crop=False)

        self._neural_network.setInput(blob)
        start = time.time()
        unconnected_layer_outputs = self._neural_network.forward(unconnected_layer_names)
        end = time.time()
        print("YOLO took {:.6f} seconds to perform detections.".format(end - start))

        self.filter_detections(unconnected_layer_outputs)

    def class_id_is_person(self, class_id):
        return self._labels[class_id] == "person"

    def detection_is_person_with_confidence(self, confidence, class_id):
        return confidence > self.confidence_threshold and self.class_id_is_person(class_id)

    def non_maxima_suppression(self):
        if len(self.detected_object_bounding_boxes) > 0:
            bounding_boxes_for_non_maxima_suppression = []
            for bounding_box in self.detected_object_bounding_boxes:
                bounding_boxes_for_non_maxima_suppression.append([bounding_box.x_start, bounding_box.y_start,
                                                                  bounding_box.width, bounding_box.height])

            detection_indices_to_keep = cv2.dnn.NMSBoxes(bounding_boxes_for_non_maxima_suppression,
                                                         self.detected_object_confidences,
                                                         self.confidence_threshold,
                                                         self._non_maxima_suppression_threshold)
            non_maxima_suppressed_bounding_boxes = []
            non_maxima_suppressed_confidences = []
            for detection_index in detection_indices_to_keep.flatten():
                non_maxima_suppressed_bounding_boxes.append(self.detected_object_bounding_boxes[detection_index])
                non_maxima_suppressed_confidences.append(self.detected_object_confidences[detection_index])
            self.detected_object_bounding_boxes = non_maxima_suppressed_bounding_boxes
            self.detected_object_confidences = non_maxima_suppressed_confidences

    def filter_detections(self, output_layers):
        def filter_confident_person_detections():
            for output in output_layers:
                for detection_properties in output:
                    detection = Detection(class_confidences=detection_properties[5:],
                                          center_coordinates=detection_properties[0:2],
                                          dimensions=detection_properties[2:4],
                                          image_width=self.image_width,
                                          image_height=self.image_height)
                    if self.detection_is_person_with_confidence(detection.most_confident_class_id_confidence,
                                                                detection.most_confident_class_id):
                        self.detected_object_bounding_boxes.append(detection.bounding_box)
                        self.detected_object_confidences.append(detection.most_confident_class_id_confidence)

        filter_confident_person_detections()
        self.non_maxima_suppression()

    def annotate_image_with_bounding_boxes_and_confidences(self):
        if len(self.detected_object_bounding_boxes) > 0:
            for index, bounding_box in enumerate(self.detected_object_bounding_boxes):
                start_coordinates = (bounding_box.x_start, bounding_box.y_start)
                end_coordinates = (bounding_box.x_end, bounding_box.y_end)
                colour = [0, 0, 200]
                cv2.rectangle(self._image,
                              start_coordinates,
                              end_coordinates,
                              colour,
                              2)
                text = "{}: {:.4f}".format("person", self.detected_object_confidences[index])
                cv2.putText(self._image,
                            text,
                            (bounding_box.x_start, bounding_box.y_start - 5),
                            cv2.FONT_HERSHEY_SIMPLEX,
                            0.5,
                            colour)

    def save_image(self, save_name):
        if self._image is not None:
            cv2.imwrite(f"{self.save_directory}/{save_name}.jpeg", image)
            print(f"Processed image saved to {self.save_directory} directory.")
        else:
            print(f"Image was None. Not saved.")


def create_yolo_trained_neural_network(yolo_files_directory):
    weights_path = os.path.sep.join([yolo_files_directory, "yolov3.weights"])
    config_path = os.path.sep.join([yolo_files_directory, "yolov3.cfg"])
    print("Creating YOLO trained neural network.")
    return cv2.dnn.readNetFromDarknet(config_path, weights_path)


if __name__ == "__main__":
    """
    Performs object detection on the provided image. Saves an annotated image if a person was detected.
    """
    image_path = sys.argv[1]
    yolo_files_directory = sys.argv[2]
    confidence_threshold = float(sys.argv[3])
    non_maxima_suppression_threshold = float(sys.argv[4])
    save_directory = sys.argv[5]
    save_name = sys.argv[6]

    labels_path = os.path.sep.join([yolo_files_directory, "coco.names"])
    yolo_labels = open(labels_path).read().strip().split("\n")

    yolo_neural_network = create_yolo_trained_neural_network(yolo_files_directory)

    image = cv2.imread(image_path)

    object_detector = ImageObjectDetector(neural_network=yolo_neural_network,
                                          labels=yolo_labels,
                                          confidence_threshold=confidence_threshold,
                                          non_maxima_suppression_threshold=non_maxima_suppression_threshold,
                                          image=image,
                                          save_directory=save_directory)

    object_detector.perform_object_detection_on_image(image)
    if len(object_detector.detected_object_bounding_boxes) > 0:
        print("Person detected.")
        object_detector.annotate_image_with_bounding_boxes_and_confidences()
        object_detector.save_image(save_name=save_name)
    else:
        print("Person not detected.")
