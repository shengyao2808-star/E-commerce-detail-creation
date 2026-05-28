import { HolderOutlined } from "@ant-design/icons";
import { Button, Empty, Space, Typography } from "antd";
import {
  closestCenter,
  DndContext,
  type DragEndEvent,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

const { Text } = Typography;

type SortableModuleBoardProps = {
  items: string[];
  onChange: (items: string[]) => void;
  emptyText: string;
  description?: string;
};

type SortableModuleItemProps = {
  id: string;
  label: string;
};

function SortableModuleItem({ id, label }: SortableModuleItemProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition
  };

  return (
    <article
      ref={setNodeRef}
      style={style}
      className={isDragging ? "module-sort-item module-sort-item--dragging" : "module-sort-item"}
    >
      <Space align="center" style={{ width: "100%", justifyContent: "space-between" }}>
        <div>
          <Text strong>{label}</Text>
          <div className="module-sort-item__meta">Drag to reorder local detail modules.</div>
        </div>
        <Button {...attributes} {...listeners} type="text" icon={<HolderOutlined />} aria-label={`Drag ${label}`} />
      </Space>
    </article>
  );
}

export function SortableModuleBoard({ items, onChange, emptyText, description }: SortableModuleBoardProps) {
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over || active.id === over.id) {
      return;
    }

    const oldIndex = items.findIndex((item) => item === active.id);
    const newIndex = items.findIndex((item) => item === over.id);

    if (oldIndex === -1 || newIndex === -1) {
      return;
    }

    onChange(arrayMove(items, oldIndex, newIndex));
  };

  if (!items.length) {
    return (
      <Empty
        description={
          <Space direction="vertical" size={4}>
            <Text>{emptyText}</Text>
            {description ? <Text type="secondary">{description}</Text> : null}
          </Space>
        }
      />
    );
  }

  return (
    <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
      <SortableContext items={items} strategy={verticalListSortingStrategy}>
        <div className="module-sort-list">
          {items.map((item) => (
            <SortableModuleItem key={item} id={item} label={item} />
          ))}
        </div>
      </SortableContext>
    </DndContext>
  );
}
